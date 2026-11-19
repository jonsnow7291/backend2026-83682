package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementación del CRUD de productos de inventario con sus reglas: código de 8 alfanuméricos
 * único, Invima obligatorio para medicamentos, precios no negativos.
 */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9]{8}$");

    /** Días de anticipación para considerar un lote "próximo a vencer". */
    public static final int DIAS_ALERTA_VENCIMIENTO = 30;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /** {@inheritDoc} */
    @Override
    public List<Medicamento> obtenerMedicamentos() {
        return medicamentoRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public List<Medicamento> listar(String filtro, String tipo, String q) {
        String f = filtro == null || filtro.isBlank() ? "TODOS" : filtro.trim().toUpperCase();
        String t = tipo == null || tipo.isBlank() ? null : tipo.trim().toUpperCase();
        String texto = q == null || q.isBlank() ? null : q.trim();
        List<Medicamento> base = switch (f) {
            case "TODOS" -> medicamentoRepository.buscar(t, texto);
            case "AGOTADOS" -> medicamentoRepository.findAllByActivoTrueAndStockActualLessThanEqualOrderByNombre(0);
            case "STOCK_BAJO" -> medicamentoRepository.stockBajo();
            case "PROXIMOS_A_VENCER" -> medicamentoRepository.proximosAVencer(LocalDate.now(),
                    LocalDate.now().plusDays(DIAS_ALERTA_VENCIMIENTO));
            case "VENCIDOS" -> medicamentoRepository.conLotesVencidos(LocalDate.now());
            default -> throw new BadRequestException("Filtro inválido. Use TODOS, AGOTADOS, STOCK_BAJO, PROXIMOS_A_VENCER o VENCIDOS");
        };
        return base.stream()
                .filter(m -> t == null || t.equals(m.getTipo()))
                .filter(m -> texto == null || m.getNombre().toLowerCase().contains(texto.toLowerCase())
                        || (m.getCodigo() != null && m.getCodigo().toLowerCase().contains(texto.toLowerCase())))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Medicamento consultar(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El producto con id " + id + " no existe"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Medicamento crear(MedicamentoRq rq) {
        lanzarSiHayErrores(validar(rq, true));
        Medicamento m = new Medicamento();
        aplicar(m, rq);
        m.setStockActual(0);
        m.setFechaIngreso(LocalDate.now());
        m.setFechaCreacionRegistro(LocalDateTime.now());
        return medicamentoRepository.save(m);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Medicamento actualizar(MedicamentoRq rq) {
        if (rq == null || rq.getId() == null) {
            throw new BadRequestException("El id del producto es obligatorio");
        }
        Medicamento m = consultar(rq.getId());
        lanzarSiHayErrores(validar(rq, false));
        if (!m.getCodigo().equalsIgnoreCase(rq.getCodigo().trim())
                && medicamentoRepository.existsByCodigoIgnoreCase(rq.getCodigo().trim())) {
            throw new BadRequestException("Ya existe un producto con el código " + rq.getCodigo());
        }
        if (!m.getNombre().equalsIgnoreCase(rq.getNombre().trim())
                && medicamentoRepository.existsByNombreIgnoreCase(rq.getNombre().trim())) {
            throw new BadRequestException("Ya existe un producto con el nombre " + rq.getNombre());
        }
        aplicar(m, rq);
        m.setFechaModificacionRegistro(LocalDateTime.now());
        return medicamentoRepository.save(m);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Medicamento cambiarEstado(Long id, boolean activo) {
        Medicamento m = consultar(id);
        m.setActivo(activo);
        m.setFechaModificacionRegistro(LocalDateTime.now());
        return medicamentoRepository.save(m);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> validar(MedicamentoRq rq, boolean nuevo) {
        List<String> e = new ArrayList<>();
        if (rq == null) {
            e.add("Datos del producto ausentes");
            return e;
        }
        if (rq.getCodigo() == null || !CODIGO.matcher(rq.getCodigo().trim()).matches()) {
            e.add("El código debe tener exactamente 8 caracteres alfanuméricos");
        } else if (nuevo && medicamentoRepository.existsByCodigoIgnoreCase(rq.getCodigo().trim())) {
            e.add("El código " + rq.getCodigo().trim() + " ya existe");
        }
        if (rq.getNombre() == null || rq.getNombre().isBlank()) {
            e.add("El nombre es obligatorio");
        } else if (rq.getNombre().trim().length() > 100) {
            e.add("El nombre supera los 100 caracteres");
        } else if (nuevo && medicamentoRepository.existsByNombreIgnoreCase(rq.getNombre().trim())) {
            e.add("El nombre '" + rq.getNombre().trim() + "' ya existe");
        }
        String tipo = rq.getTipo() == null ? "" : rq.getTipo().trim().toUpperCase();
        if (!tipo.equals("MEDICAMENTO") && !tipo.equals("UTENSILIO")) {
            e.add("El tipo debe ser MEDICAMENTO o UTENSILIO");
        } else if (tipo.equals("MEDICAMENTO") && (rq.getRegistroInvima() == null || rq.getRegistroInvima().isBlank())) {
            e.add("El registro Invima es obligatorio para medicamentos");
        }
        if (rq.getPrecioCompra() == null || rq.getPrecioCompra().signum() < 0) {
            e.add("El precio de compra es obligatorio y no puede ser negativo");
        }
        if (rq.getPrecioVenta() == null || rq.getPrecioVenta().signum() < 0) {
            e.add("El precio de venta es obligatorio y no puede ser negativo");
        }
        if (rq.getIvaPorcentaje() != null
                && (rq.getIvaPorcentaje().signum() < 0 || rq.getIvaPorcentaje().compareTo(new BigDecimal("100")) > 0)) {
            e.add("El IVA debe estar entre 0 y 100");
        }
        if (rq.getStockMinimo() != null && rq.getStockMinimo() < 0) {
            e.add("El stock mínimo no puede ser negativo");
        }
        return e;
    }

    private void lanzarSiHayErrores(List<String> errores) {
        if (!errores.isEmpty()) {
            throw new BadRequestException(String.join("; ", errores));
        }
    }

    private void aplicar(Medicamento m, MedicamentoRq rq) {
        m.setCodigo(rq.getCodigo().trim().toUpperCase());
        m.setNombre(rq.getNombre().trim());
        m.setDescripcion(rq.getDescripcion());
        m.setPresentacion(rq.getPresentacion());
        m.setTipo(rq.getTipo().trim().toUpperCase());
        m.setRegistroInvima(rq.getRegistroInvima() == null || rq.getRegistroInvima().isBlank() ? null : rq.getRegistroInvima().trim());
        m.setPrecioCompra(rq.getPrecioCompra());
        m.setPrecioVenta(rq.getPrecioVenta());
        m.setIvaPorcentaje(rq.getIvaPorcentaje() == null ? BigDecimal.ZERO : rq.getIvaPorcentaje());
        m.setStockMinimo(rq.getStockMinimo() == null ? 0 : rq.getStockMinimo());
        m.setFechaProximoPedido(rq.getFechaProximoPedido());
        m.setActivo(rq.getActivo() == null ? Boolean.TRUE : rq.getActivo());
    }
}
