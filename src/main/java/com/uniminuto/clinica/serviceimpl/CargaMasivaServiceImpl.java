package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CargaMasiva;
import com.uniminuto.clinica.entity.CargaMasivaDetalle;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.CargaMasivaRs;
import com.uniminuto.clinica.models.FilaCargaRs;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.repository.CargaMasivaDetalleRepository;
import com.uniminuto.clinica.repository.CargaMasivaRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.CargaMasivaService;
import com.uniminuto.clinica.service.InventarioService;
import com.uniminuto.clinica.service.MedicamentoService;
import com.uniminuto.clinica.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación de la carga masiva: valida encabezados, obligatorios, duplicados (código y nombre,
 * en el archivo y en la BD), precios, cantidades y fechas. Cada fila válida se importa de forma
 * aislada para que un error no afecte a las demás.
 */
@Service
public class CargaMasivaServiceImpl implements CargaMasivaService {

    private static final List<String> OBLIGATORIAS = List.of("codigo", "nombre", "tipo", "registro_invima",
            "precio_compra", "precio_venta", "iva_porcentaje", "stock_minimo", "numero_lote", "cantidad",
            "fecha_compra", "fecha_vencimiento");

    @Autowired
    private MedicamentoService medicamentoService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private CargaMasivaRepository cargaRepository;

    @Autowired
    private CargaMasivaDetalleRepository detalleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UsuarioActual usuarioActual;

    /** Fila ya interpretada del archivo, con sus errores. */
    private static class Fila {
        int numero;
        MedicamentoRq producto = new MedicamentoRq();
        LoteRq lote = new LoteRq();
        List<String> errores = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public CargaMasivaRs previsualizar(MultipartFile archivo) {
        List<Fila> filas = analizar(archivo);
        return resumen(archivo, filas, null);
    }

    /** {@inheritDoc} */
    @Override
    public CargaMasivaRs confirmar(MultipartFile archivo) {
        List<Fila> filas = analizar(archivo);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        for (Fila f : filas) {
            if (!f.errores.isEmpty()) {
                continue;
            }
            try {
                tx.executeWithoutResult(status -> {
                    var producto = medicamentoService.crear(f.producto);
                    if (f.lote.getCantidad() != null && f.lote.getCantidad() > 0) {
                        f.lote.setMedicamentoId(producto.getId());
                        inventarioService.registrarEntrada(f.lote);
                    }
                });
            } catch (RuntimeException e) {
                f.errores.add("No se pudo importar: " + e.getMessage());
            }
        }
        CargaMasivaRs rs = resumen(archivo, filas, null);
        CargaMasiva carga = new CargaMasiva();
        carga.setNombreArchivo(archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename());
        carga.setUsuarioId(usuarioActual.id());
        carga.setFecha(LocalDateTime.now());
        carga.setTotalFilas(rs.getTotalFilas());
        carga.setFilasValidas(rs.getValidas());
        carga.setFilasRechazadas(rs.getRechazadas());
        carga = cargaRepository.save(carga);
        for (Fila f : filas) {
            CargaMasivaDetalle d = new CargaMasivaDetalle();
            d.setCargaId(carga.getId());
            d.setFila(f.numero);
            d.setCodigo(f.producto.getCodigo());
            d.setResultado(f.errores.isEmpty() ? "IMPORTADA" : "RECHAZADA");
            String motivo = String.join("; ", f.errores);
            d.setMotivo(motivo.length() > 500 ? motivo.substring(0, 500) : motivo);
            detalleRepository.save(d);
        }
        rs.setCargaId(carga.getId());
        return rs;
    }

    /** {@inheritDoc} */
    @Override
    public List<CargaMasiva> historial() {
        return cargaRepository.findAllByOrderByFechaDesc();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] informeErrores(Long cargaId) {
        CargaMasiva c = cargaRepository.findById(cargaId)
                .orElseThrow(() -> new NotFoundException("La carga " + cargaId + " no existe"));
        List<List<Object>> filas = detalleRepository.findAllByCargaIdOrderByFila(cargaId).stream()
                .filter(d -> "RECHAZADA".equals(d.getResultado()))
                .map(d -> List.<Object>of(d.getFila(), d.getCodigo() == null ? "" : d.getCodigo(), d.getMotivo()))
                .toList();
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("Archivo", c.getNombreArchivo());
        totales.put("Filas rechazadas", filas.size());
        return ExcelUtil.generar("Errores", "Informe de errores - carga " + cargaId,
                List.of("Fila", "Código", "Motivo"), filas, totales);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] plantilla(String formato) {
        List<String> cols = new ArrayList<>(OBLIGATORIAS);
        cols.add("descripcion");
        cols.add("presentacion");
        List<List<Object>> ejemplo = List.of(
                List.of("AMX00010", "Amoxicilina 500mg", "MEDICAMENTO", "INVIMA-2025-100", 9000, 15000, 0, 10, "L-2026-01", 50, "2026-11-01", "2028-11-01", "Antibiótico", "Caja x10"),
                List.of("GUA00001", "Guantes de nitrilo", "UTENSILIO", "", 20000, 32000, 19, 5, "L-GU-01", 20, "2026-11-01", "2029-11-01", "Guantes", "Caja x100"));
        if ("csv".equalsIgnoreCase(formato)) {
            StringBuilder sb = new StringBuilder("﻿").append(String.join(",", cols)).append("\n");
            for (List<Object> f : ejemplo) {
                sb.append(String.join(",", f.stream().map(String::valueOf).toList())).append("\n");
            }
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }
        return ExcelUtil.generar("Inventario", null, cols, ejemplo, null);
    }

    private CargaMasivaRs resumen(MultipartFile archivo, List<Fila> filas, Long cargaId) {
        CargaMasivaRs rs = new CargaMasivaRs();
        rs.setCargaId(cargaId);
        rs.setArchivo(archivo.getOriginalFilename());
        rs.setTotalFilas(filas.size());
        for (Fila f : filas) {
            boolean ok = f.errores.isEmpty();
            rs.getFilas().add(new FilaCargaRs(f.numero, f.producto.getCodigo(), f.producto.getNombre(), ok, f.errores));
            if (ok) {
                rs.setValidas(rs.getValidas() + 1);
            } else {
                rs.setRechazadas(rs.getRechazadas() + 1);
            }
        }
        return rs;
    }

    /**
     * Lee el archivo y valida cada fila.
     */
    private List<Fila> analizar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Debe adjuntar un archivo .xlsx o .csv con datos");
        }
        List<List<String>> datos;
        try {
            datos = ExcelUtil.leer(archivo.getOriginalFilename(), archivo.getInputStream());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException("No se pudo leer el archivo: verifique que sea un .xlsx o .csv válido");
        }
        if (datos.size() < 2) {
            throw new BadRequestException("El archivo no contiene filas de datos");
        }
        List<String> encabezados = datos.get(0).stream().map(h -> h.trim().toLowerCase().replace(' ', '_')).toList();
        List<String> faltan = OBLIGATORIAS.stream().filter(h -> !encabezados.contains(h)).toList();
        if (!faltan.isEmpty()) {
            throw new BadRequestException("Faltan columnas obligatorias: " + String.join(", ", faltan));
        }
        Set<String> codigos = new HashSet<>();
        Set<String> nombres = new HashSet<>();
        List<Fila> filas = new ArrayList<>();
        for (int i = 1; i < datos.size(); i++) {
            List<String> celdas = datos.get(i);
            if (celdas.stream().allMatch(String::isBlank)) {
                continue;
            }
            Map<String, String> v = new HashMap<>();
            for (int c = 0; c < encabezados.size(); c++) {
                v.put(encabezados.get(c), c < celdas.size() ? celdas.get(c) : "");
            }
            Fila f = new Fila();
            f.numero = i + 1;
            interpretar(f, v);
            if (f.errores.isEmpty() || f.producto.getCodigo() != null) {
                String cod = f.producto.getCodigo() == null ? "" : f.producto.getCodigo().trim().toUpperCase();
                String nom = f.producto.getNombre() == null ? "" : f.producto.getNombre().trim().toLowerCase();
                if (!cod.isEmpty() && !codigos.add(cod)) {
                    f.errores.add("Código duplicado dentro del archivo");
                }
                if (!nom.isEmpty() && !nombres.add(nom)) {
                    f.errores.add("Nombre duplicado dentro del archivo");
                }
            }
            filas.add(f);
        }
        if (filas.isEmpty()) {
            throw new BadRequestException("El archivo no contiene filas de datos");
        }
        return filas;
    }

    private void interpretar(Fila f, Map<String, String> v) {
        MedicamentoRq p = f.producto;
        p.setCodigo(v.get("codigo"));
        p.setNombre(v.get("nombre"));
        p.setTipo(v.get("tipo"));
        p.setRegistroInvima(v.get("registro_invima"));
        p.setDescripcion(v.get("descripcion"));
        p.setPresentacion(v.get("presentacion"));
        p.setPrecioCompra(decimal(f, v, "precio_compra"));
        p.setPrecioVenta(decimal(f, v, "precio_venta"));
        p.setIvaPorcentaje(decimal(f, v, "iva_porcentaje"));
        p.setStockMinimo(entero(f, v, "stock_minimo"));
        f.errores.addAll(medicamentoService.validar(p, true));
        Integer cantidad = entero(f, v, "cantidad");
        LocalDate compra = fecha(f, v, "fecha_compra");
        LocalDate vence = fecha(f, v, "fecha_vencimiento");
        LoteRq l = f.lote;
        l.setCantidad(cantidad);
        l.setNumeroLote(v.get("numero_lote"));
        l.setPrecioCompra(p.getPrecioCompra());
        l.setFechaCompra(compra);
        l.setFechaVencimiento(vence);
        if (cantidad != null && cantidad < 0) {
            f.errores.add("La cantidad no puede ser negativa");
        }
        if (cantidad != null && cantidad > 0) {
            if (l.getNumeroLote() == null || l.getNumeroLote().isBlank()) {
                f.errores.add("El número de lote es obligatorio cuando la cantidad es mayor que cero");
            }
            if (compra == null) {
                f.errores.add("La fecha de compra es obligatoria cuando la cantidad es mayor que cero");
            }
            if (vence == null && "MEDICAMENTO".equalsIgnoreCase(p.getTipo() == null ? "" : p.getTipo().trim())) {
                f.errores.add("La fecha de vencimiento es obligatoria para los medicamentos (solo los utensilios pueden omitirla)");
            }
            if (compra != null && vence != null && !vence.isAfter(compra)) {
                f.errores.add("La fecha de vencimiento debe ser posterior a la fecha de compra");
            }
        }
    }

    private BigDecimal decimal(Fila f, Map<String, String> v, String campo) {
        String t = v.get(campo);
        if (t == null || t.isBlank()) {
            if (!campo.equals("iva_porcentaje")) {
                f.errores.add("El campo " + campo + " es obligatorio");
            }
            return null;
        }
        try {
            return new BigDecimal(t.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            f.errores.add("El campo " + campo + " no es un número válido: '" + t + "'");
            return null;
        }
    }

    private Integer entero(Fila f, Map<String, String> v, String campo) {
        String t = v.get(campo);
        if (t == null || t.isBlank()) {
            f.errores.add("El campo " + campo + " es obligatorio");
            return null;
        }
        try {
            return new BigDecimal(t.trim().replace(',', '.')).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            f.errores.add("El campo " + campo + " debe ser un entero: '" + t + "'");
            return null;
        }
    }

    private LocalDate fecha(Fila f, Map<String, String> v, String campo) {
        String t = v.get(campo);
        if (t == null || t.isBlank()) {
            return null;
        }
        try {
            return ExcelUtil.fecha(t);
        } catch (RuntimeException e) {
            f.errores.add("El campo " + campo + " debe tener formato aaaa-mm-dd: '" + t + "'");
            return null;
        }
    }
}
