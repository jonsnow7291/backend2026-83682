package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                "BAD REQUEST",
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    /**
     * Maneja la ausencia de un parámetro de solicitud obligatorio (ej. una fecha de filtro
     * no enviada), devolviendo un HTTP 400 en vez de un error interno del servidor.
     *
     * @param ex excepción lanzada por Spring cuando falta un {@code @RequestParam} obligatorio.
     * @return respuesta HTTP 400 con el detalle del error.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                "El parámetro '" + ex.getParameterName() + "' es obligatorio"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja un parámetro de solicitud con un formato/tipo inválido (ej. una fecha mal
     * formateada), devolviendo un HTTP 400 en vez de un error interno del servidor.
     *
     * @param ex excepción lanzada por Spring cuando no puede convertir un parámetro al tipo esperado.
     * @return respuesta HTTP 400 con el detalle del error.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                "El parámetro '" + ex.getName() + "' tiene un formato inválido"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL SERVER_ERROR",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
