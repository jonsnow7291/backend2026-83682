package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de productos de inventario; delega en {@link MedicamentoService}.
 */
@RestController
public class MedicamentoApiController implements MedicamentoApi {

    @Autowired
    private MedicamentoService medicamentoService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.obtenerMedicamentos());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listar(String filtro, String tipo, String q) {
        return ResponseEntity.ok(medicamentoService.listar(filtro, tipo, q));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Medicamento> consultar(Long id) {
        return ResponseEntity.ok(medicamentoService.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Medicamento> crear(MedicamentoRq rq) {
        return ResponseEntity.ok(medicamentoService.crear(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Medicamento> actualizar(MedicamentoRq rq) {
        return ResponseEntity.ok(medicamentoService.actualizar(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Medicamento> cambiarEstado(Long id, boolean activo) {
        return ResponseEntity.ok(medicamentoService.cambiarEstado(id, activo));
    }
}
