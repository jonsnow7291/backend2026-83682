package com.uniminuto.clinica.reglas;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.FacturaDtos;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.DespachoService;
import com.uniminuto.clinica.service.LineasFactura;
import com.uniminuto.clinica.serviceimpl.EspecializacionServiceImpl;
import com.uniminuto.clinica.serviceimpl.FacturaServiceImpl;
import com.uniminuto.clinica.serviceimpl.FormulaMedicaServiceImpl;
import com.uniminuto.clinica.serviceimpl.InventarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de las reglas añadidas al alinear el sistema con el enunciado: confirmación de la fórmula
 * (5.8-2), cantidad total calculada (1.3), vencimiento opcional para utensilios (1.1/1.2),
 * descuentos autorizados y despacho previo a la factura (3.4/3.5).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReglasAlineacionEnunciadoTest {

    @Mock FormulaMedicaRepository formulaRepository;
    @Mock CitaRepository citaRepository;
    @Mock MedicamentoRepository medicamentoRepository;
    @Mock AtencionRepository atencionRepository;
    @Mock DespachoRepository despachoRepository;
    @Mock LoteRepository loteRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock DespachoService despachoService;
    @Mock UsuarioActual usuarioActual;
    @Mock FacturaRepository facturaRepository;
    @Mock TarifaRepository tarifaRepository;
    @Mock LineasFactura lineasFactura;
    @Mock EspecializacionRepository especializacionRepository;
    @InjectMocks EspecializacionServiceImpl especialidades;
    @InjectMocks FormulaMedicaServiceImpl formulas;
    @InjectMocks FacturaServiceImpl facturas;
    @InjectMocks InventarioServiceImpl inventario;

    private Cita citaAtendida() {
        Medico medico = new Medico();
        medico.setId(1L);
        medico.setEspecializacionId(1);
        Cita c = new Cita();
        c.setId(7L);
        c.setEstado("ATENDIDA");
        c.setMedico(medico);
        return c;
    }

    private Medicamento medicamento(String tipo) {
        Medicamento m = new Medicamento();
        m.setId(1L);
        m.setNombre("Amoxicilina");
        m.setTipo(tipo);
        m.setActivo(true);
        return m;
    }

    @Test
    void laFormulaNaceEnBorradorYNoGeneraDespachoHastaConfirmarla() {
        when(citaRepository.findById(7L)).thenReturn(Optional.of(citaAtendida()));
        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(medicamento("MEDICAMENTO")));
        when(formulaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        FormulaMedicaRq rq = new FormulaMedicaRq();
        rq.setCitaId(7L);
        rq.setDetalles(List.of(detalle(null, null, 4)));

        formulas.guardarFormulaMedica(rq);

        ArgumentCaptor<FormulaMedica> captor = ArgumentCaptor.forClass(FormulaMedica.class);
        verify(formulaRepository).save(captor.capture());
        assertEquals("BORRADOR", captor.getValue().getEstado());
        verifyNoInteractions(despachoService);
    }

    @Test
    void confirmarCreaElDespachoUnaSolaVez() {
        FormulaMedica f = new FormulaMedica();
        f.setId(3L);
        f.setCita(citaAtendida());
        f.getDetalles().add(new FormulaMedicaDetalle());
        when(formulaRepository.findById(3L)).thenReturn(Optional.of(f));
        when(formulaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        formulas.confirmarFormulaMedica(3L);

        assertEquals("CONFIRMADA", f.getEstado());
        assertEquals("PENDIENTE_DESPACHO", f.getEstadoDespacho());
        assertNotNull(f.getFechaConfirmacion());
        verify(despachoService, times(1)).crearParaFormula(f);
        assertThrows(BadRequestException.class, () -> formulas.confirmarFormulaMedica(3L),
                "una fórmula ya confirmada no puede confirmarse de nuevo (no se duplica el despacho)");
        verify(despachoService, times(1)).crearParaFormula(f);
    }

    @Test
    void laCantidadTotalSeCalculaConLaFormulacion() {
        when(citaRepository.findById(7L)).thenReturn(Optional.of(citaAtendida()));
        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(medicamento("MEDICAMENTO")));
        when(formulaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        FormulaMedicaRq rq = new FormulaMedicaRq();
        rq.setCitaId(7L);
        // 2 unidades x 3 tomas/día x 5 días = 30; la cantidad manual (1) se ignora
        rq.setDetalles(List.of(detalle(2, 3, 1)));

        formulas.guardarFormulaMedica(rq);

        ArgumentCaptor<FormulaMedica> captor = ArgumentCaptor.forClass(FormulaMedica.class);
        verify(formulaRepository).save(captor.capture());
        assertEquals(30, captor.getValue().getDetalles().get(0).getCantidadPrescrita());
    }

    private FormulaMedicaRq.Detalle detalle(Integer unidades, Integer tomas, Integer cantidad) {
        FormulaMedicaRq.Detalle d = new FormulaMedicaRq.Detalle();
        d.setMedicamentoId(1L);
        d.setDosis("1 tableta");
        d.setFrecuencia("cada 8 horas");
        d.setDuracionDias(5);
        d.setUnidadesPorDosis(unidades);
        d.setDosisPorDia(tomas);
        d.setCantidadPrescrita(cantidad);
        return d;
    }

    private LoteRq loteSinVencimiento() {
        LoteRq rq = new LoteRq();
        rq.setMedicamentoId(1L);
        rq.setNumeroLote("L-1");
        rq.setCantidad(10);
        rq.setFechaCompra(LocalDate.now());
        return rq;
    }

    @Test
    void elUtensilioAdmiteLoteSinVencimientoPeroElMedicamentoNo() {
        when(loteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(medicamento("UTENSILIO")));
        LoteMedicamento l = inventario.registrarEntrada(loteSinVencimiento());
        assertNull(l.getFechaVencimiento());

        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(medicamento("MEDICAMENTO")));
        assertThrows(BadRequestException.class, () -> inventario.registrarEntrada(loteSinVencimiento()));
    }

    private void prepararFacturaDeCitaAtendida() {
        Cita c = citaAtendida();
        when(citaRepository.bloquear(7L)).thenReturn(Optional.of(c));
        Tarifa t = new Tarifa();
        t.setId(1L);
        when(tarifaRepository.findAllByEspecializacionIdAndActivaTrueOrderByIdAsc(1L)).thenReturn(List.of(t));
        when(lineasFactura.servicio(any())).thenReturn(new FacturaDetalle());
        when(lineasFactura.medicamentos(7L, true)).thenReturn(List.of());
        when(lineasFactura.subtotal(any())).thenReturn(new BigDecimal("100"));
        when(lineasFactura.impuestos(any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void soloUnAdminPuedeAutorizarDescuentos() {
        prepararFacturaDeCitaAtendida();
        when(usuarioActual.tieneRol(Roles.ADMIN)).thenReturn(false);
        assertThrows(AccessDeniedException.class,
                () -> facturas.generar(new FacturaDtos.GenerarRq(7L, new BigDecimal("10"))));
    }

    @Test
    void noSeFacturaMientrasLaFormulaSigueSinDespachar() {
        prepararFacturaDeCitaAtendida();
        FormulaMedica f = new FormulaMedica();
        f.setId(3L);
        Despacho d = new Despacho();
        d.setEstado("PENDIENTE_DESPACHO");
        when(formulaRepository.findAllByCita_IdOrderByIdAsc(7L)).thenReturn(List.of(f));
        when(despachoRepository.findByFormulaId(3L)).thenReturn(Optional.of(d));
        assertThrows(ConflictException.class, () -> facturas.generar(new FacturaDtos.GenerarRq(7L, null)));
    }

    @Test
    void laEspecialidadExigeNombreYCodigoValidosYUnicos() {
        Especializacion existente = new Especializacion();
        existente.setId(1L);
        existente.setNombre("Cirugía");
        existente.setCodigoEspecializacion("CIR");
        when(especializacionRepository.findAll()).thenReturn(List.of(existente));
        when(especializacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EspecializacionRq rq = new EspecializacionRq();
        rq.setNombre("Dermatología");
        rq.setCodigoEspecializacion("codigo demasiado largo!");
        assertThrows(BadRequestException.class, () -> especialidades.guardar(rq), "código inválido");

        rq.setCodigoEspecializacion("cir");
        assertThrows(ConflictException.class, () -> especialidades.guardar(rq), "código repetido (sin distinguir mayúsculas)");

        rq.setCodigoEspecializacion("DER");
        Especializacion creada = especialidades.guardar(rq);
        assertEquals("DER", creada.getCodigoEspecializacion());
        assertTrue(creada.getActiva(), "una especialidad nueva nace activa");
    }
}
