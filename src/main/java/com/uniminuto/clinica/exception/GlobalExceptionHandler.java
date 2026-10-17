package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejador centralizado de errores: traduce las excepciones a respuestas JSON
 * homogéneas ({@link ErrorResponse}) con el código HTTP adecuado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger para registrar los errores no controlados sin enviarlos al cliente.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las violaciones de reglas de negocio y validaciones, devolviendo un HTTP 400
     * con el mensaje de la excepción.
     *
     * @param ex excepción de negocio lanzada por la capa de servicios.
     * @return respuesta HTTP 400 con el detalle del error.
     */
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

    /**
     * Maneja un cuerpo JSON mal formado o con valores de tipo inválido (ej. una fecha
     * con formato incorrecto), devolviendo un HTTP 400 en vez de un error interno.
     *
     * @param ex excepción lanzada por Spring cuando no puede leer el cuerpo de la solicitud.
     * @return respuesta HTTP 400 con un mensaje comprensible.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                "La informacion enviada no tiene un formato valido"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja el uso de un método HTTP no soportado por el endpoint (ej. POST sobre un PUT),
     * devolviendo un HTTP 405.
     *
     * @param ex excepción lanzada por Spring cuando el método HTTP no es soportado.
     * @return respuesta HTTP 405 con un mensaje comprensible.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD NOT ALLOWED",
                "El metodo " + ex.getMethod() + " no esta permitido para este servicio"
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error);
    }

    /**
     * Maneja cualquier error no controlado. Registra el detalle en el log del servidor
     * y responde un mensaje genérico, sin exponer información técnica al usuario.
     *
     * @param ex excepción no controlada.
     * @return respuesta HTTP 500 con un mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        LOGGER.error("Error no controlado", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL SERVER_ERROR",
                "Ocurrio un error inesperado. Intente nuevamente o contacte al administrador"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
