package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.TarifaApi;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.TarifaLog;
import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.service.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de tarifas.
 */
@RestController
public class TarifaApiController implements TarifaApi {

    @Autowired
    private TarifaService tarifaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Tarifa>> listar(Long especializacionId, boolean todas) {
        return ResponseEntity.ok(tarifaService.listar(especializacionId, todas));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Tarifa> crear(TarifaRq rq) {
        return ResponseEntity.ok(tarifaService.crear(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Tarifa> actualizar(TarifaRq rq) {
        return ResponseEntity.ok(tarifaService.actualizar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Tarifa> cambiarEstado(Long id, boolean activa) {
        return ResponseEntity.ok(tarifaService.cambiarEstado(id, activa));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<TarifaLog>> historial(Long id) {
        return ResponseEntity.ok(tarifaService.historial(id));
    }
}
