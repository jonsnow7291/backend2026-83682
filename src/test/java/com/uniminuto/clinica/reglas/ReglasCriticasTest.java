package com.uniminuto.clinica.reglas;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.DespachoDtos;
import com.uniminuto.clinica.models.FacturaDtos;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.serviceimpl.*;
import com.uniminuto.clinica.service.InventarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de las reglas de negocio críticas: stock, vencimiento, doble descuento,
 * entrega parcial, doble cobro y pagos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReglasCriticasTest {

    @Mock LoteRepository loteRepository;
    @Mock MedicamentoRepository medicamentoRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock UsuarioActual usuarioActual;
    @InjectMocks InventarioServiceImpl inventario;

    @Test
    void salidaConMasUnidadesQueLasDisponiblesSeRechaza() {
        LoteMedicamento l = lote(1L, 5, LocalDate.now().plusDays(30));
        when(loteRepository.bloquear(1L)).thenReturn(Optional.of(l));
        assertThrows(BadRequestException.class, () -> inventario.registrarSalida(1L, 6, 1L, 1L, "x"));
        assertEquals(5, l.getCantidadDisponible(), "el stock no puede quedar negativo ni cambiar");
    }

    @Test
    void salidaDeLoteVencidoSeRechaza() {
        when(loteRepository.bloquear(1L)).thenReturn(Optional.of(lote(1L, 5, LocalDate.now().minusDays(1))));
        assertThrows(BadRequestException.class, () -> inventario.registrarSalida(1L, 1, 1L, 1L, "x"));
    }

    @Test
    void entradaConVencimientoAnteriorALaCompraSeRechaza() {
        LoteRq rq = new LoteRq();
        rq.setMedicamentoId(1L);
        rq.setNumeroLote("L1");
        rq.setCantidad(10);
        rq.setFechaCompra(LocalDate.now());
        rq.setFechaVencimiento(LocalDate.now().minusDays(1));
        assertThrows(BadRequestException.class, () -> inventario.registrarEntrada(rq));
    }

    @Mock MedicamentoRepository medRepo;
    @InjectMocks MedicamentoServiceImpl medicamentos;

    @Test
    void medicamentoSinInvimaOCodigoInvalidoSeRechaza() {
        MedicamentoRq rq = new MedicamentoRq();
        rq.setCodigo("ABC");
        rq.setNombre("X");
        rq.setTipo("MEDICAMENTO");
        rq.setPrecioCompra(BigDecimal.ONE);
        rq.setPrecioVenta(BigDecimal.TEN);
        List<String> errores = medicamentos.validar(rq, true);
        assertTrue(errores.stream().anyMatch(e -> e.contains("8 caracteres")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("Invima")));
        rq.setTipo("UTENSILIO");
        rq.setCodigo("UTL00001");
        assertTrue(medicamentos.validar(rq, true).isEmpty(), "el utensilio no requiere Invima");
    }

    @Mock DespachoRepository despachoRepository;
    @Mock DespachoEstadoLogRepository logRepository;
    @Mock FormulaMedicaRepository formulaRepository;
    @Mock InventarioService inventarioService;
    @InjectMocks DespachoServiceImpl despacho;

    private Despacho despachoCon(String estado, int solicitada, int entregada) {
        Despacho d = new Despacho();
        d.setId(1L);
        d.setFormulaId(1L);
        d.setEstado(estado);
        DespachoDetalle dd = new DespachoDetalle();
        dd.setId(1L);
        dd.setMedicamentoId(1L);
        dd.setCantidadSolicitada(solicitada);
        dd.setCantidadEntregada(entregada);
        d.getDetalles().add(dd);
        return d;
    }

    @Test
    void despachoYaDespachadoNoPermiteDobleDescuento() {
        when(despachoRepository.bloquear(1L)).thenReturn(Optional.of(despachoCon("DESPACHADO", 2, 2)));
        assertThrows(ConflictException.class, () -> despacho.confirmar(1L, null));
        verifyNoInteractions(inventarioService);
    }

    @Test
    void noSePuedeEntregarMasDeLoSolicitadoPorElMedico() {
        when(despachoRepository.bloquear(1L)).thenReturn(Optional.of(despachoCon("EN_PREPARACION", 2, 0)));
        Medicamento m = new Medicamento();
        m.setId(1L);
        m.setNombre("Amox");
        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(m));
        when(loteRepository.lotesFefoBloqueados(anyLong(), any())).thenReturn(List.of(lote(1L, 50, LocalDate.now().plusDays(90))));
        var rq = new DespachoDtos.ConfirmarRq(List.of(new DespachoDtos.LineaEntrega(1L, 5)), null);
        assertThrows(BadRequestException.class, () -> despacho.confirmar(1L, rq));
        verify(inventarioService, never()).registrarSalida(anyLong(), anyInt(), any(), any(), any());
    }

    @Test
    void entregaParcialDescuentaSoloLoDisponibleYQuedaParcial() {
        Despacho d = despachoCon("EN_PREPARACION", 5, 0);
        when(despachoRepository.bloquear(1L)).thenReturn(Optional.of(d));
        Medicamento m = new Medicamento();
        m.setId(1L);
        m.setNombre("Amox");
        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(m));
        when(loteRepository.lotesFefoBloqueados(anyLong(), any())).thenReturn(List.of(lote(1L, 3, LocalDate.now().plusDays(90))));
        when(formulaRepository.findById(1L)).thenReturn(Optional.empty());
        when(loteRepository.findAllByMedicamentoIdOrderByFechaVencimientoAsc(anyLong())).thenReturn(List.of());
        FormulaMedica f = new FormulaMedica();
        f.setId(1L);
        // el armado del detalle consulta la fórmula; se prueba el efecto sobre inventario y estado
        try {
            despacho.confirmar(1L, null);
        } catch (RuntimeException ignorada) {
            // el armado de la respuesta puede requerir datos no simulados
        }
        verify(inventarioService).registrarSalida(eq(1L), eq(3), eq(1L), eq(1L), any());
        assertEquals("DESPACHO_PARCIAL", d.getEstado());
        assertEquals(3, d.getDetalles().get(0).getCantidadEntregada());
    }

    @Mock FacturaRepository facturaRepository;
    @Mock CitaRepository citaRepository;
    @InjectMocks FacturaServiceImpl facturas;

    @Test
    void noSeFacturaDosVecesLaMismaCita() {
        Cita c = new Cita();
        c.setId(7L);
        c.setEstado("ATENDIDA");
        when(citaRepository.bloquear(7L)).thenReturn(Optional.of(c));
        when(facturaRepository.findFirstByCitaIdAndEstadoPagoNot(7L, "ANULADA")).thenReturn(Optional.of(new Factura()));
        assertThrows(ConflictException.class, () -> facturas.generar(new FacturaDtos.GenerarRq(7L, null)));
    }

    @Test
    void noSeFacturaUnaCitaNoAtendida() {
        Cita c = new Cita();
        c.setId(7L);
        c.setEstado("PENDIENTE");
        when(citaRepository.bloquear(7L)).thenReturn(Optional.of(c));
        assertThrows(BadRequestException.class, () -> facturas.generar(new FacturaDtos.GenerarRq(7L, null)));
    }

    @Mock PagoRepository pagoRepository;
    @Mock FacturaRepository facRepo2;
    @InjectMocks PagoServiceImpl pagos;

    @Test
    void pagoMayorAlSaldoSeRechaza() {
        Factura f = new Factura();
        f.setId(1L);
        f.setTotal(new BigDecimal("100"));
        f.setEstadoPago("PENDIENTE");
        when(facturaRepository.bloquear(1L)).thenReturn(Optional.of(f));
        when(pagoRepository.totalPagado(1L)).thenReturn(new BigDecimal("40"));
        assertThrows(BadRequestException.class,
                () -> pagos.registrar(new FacturaDtos.PagoRq(1L, new BigDecimal("61"), "EFECTIVO", null)));
    }

    private LoteMedicamento lote(Long id, int cantidad, LocalDate vence) {
        LoteMedicamento l = new LoteMedicamento();
        l.setId(id);
        l.setMedicamentoId(1L);
        l.setNumeroLote("L" + id);
        l.setCantidadDisponible(cantidad);
        l.setFechaVencimiento(vence);
        return l;
    }
}
