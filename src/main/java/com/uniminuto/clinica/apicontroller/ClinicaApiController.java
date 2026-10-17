package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClinicaApi;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementación REST de {@link ClinicaApi} con los endpoints de diagnóstico.
 */
@RestController
public class ClinicaApiController implements ClinicaApi {

    /**
     * Servicio de diagnóstico.
     */
    @Autowired
    private ClinicaService clinicaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> testService() throws BadRequestException {
        return ResponseEntity.ok("Servicio funcionando correctamente");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> testService2() throws BadRequestException {
        return ResponseEntity.ok(clinicaService.testService2());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> testService3() throws BadRequestException {
        return ResponseEntity.ok(clinicaService.testService3());
    }

}
