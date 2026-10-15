package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone la consulta de medicamentos del inventario.
 */
@RestController
public class MedicamentoApiController implements MedicamentoApi {

    /**
     * Servicio con la lógica de negocio de medicamentos del inventario.
     */
    @Autowired
    private MedicamentoService medicamentoService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.obtenerMedicamentos());
    }
}
