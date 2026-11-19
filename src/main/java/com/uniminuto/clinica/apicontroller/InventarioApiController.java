package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.InventarioApi;
import com.uniminuto.clinica.entity.CargaMasiva;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.models.CargaMasivaRs;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.service.CargaMasivaService;
import com.uniminuto.clinica.service.InventarioService;
import com.uniminuto.clinica.util.Descargas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST de lotes, movimientos y carga masiva.
 */
@RestController
public class InventarioApiController implements InventarioApi {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private CargaMasivaService cargaMasivaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<LoteMedicamento> entrada(LoteRq rq) {
        return ResponseEntity.ok(inventarioService.registrarEntrada(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<LoteMedicamento>> lotes(Long medicamentoId) {
        return ResponseEntity.ok(inventarioService.listarLotes(medicamentoId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MovimientoInventario>> movimientos(Long medicamentoId) {
        return ResponseEntity.ok(inventarioService.listarMovimientos(medicamentoId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CargaMasivaRs> previsualizar(MultipartFile archivo) {
        return ResponseEntity.ok(cargaMasivaService.previsualizar(archivo));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CargaMasivaRs> confirmar(MultipartFile archivo) {
        return ResponseEntity.ok(cargaMasivaService.confirmar(archivo));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CargaMasiva>> historial() {
        return ResponseEntity.ok(cargaMasivaService.historial());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> errores(Long id) {
        return Descargas.xlsx("errores-carga-" + id + ".xlsx", cargaMasivaService.informeErrores(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> plantilla(String formato) {
        byte[] datos = cargaMasivaService.plantilla(formato);
        if ("csv".equalsIgnoreCase(formato)) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=plantilla-inventario.csv")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(datos);
        }
        return Descargas.xlsx("plantilla-inventario.xlsx", datos);
    }
}
