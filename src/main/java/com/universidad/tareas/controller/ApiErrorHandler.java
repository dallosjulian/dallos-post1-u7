package com.universidad.tareas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para controladores REST (@RestControllerAdvice).
 * Intercepta fallos de validación de Bean Validation (@Valid) en cuerpos JSON
 * y transforma los errores en una estructura JSON de mapa (campo -> mensaje de error)
 * con código de estado HTTP 400 Bad Request.
 *
 * @author Dallos
 */
@RestControllerAdvice(assignableTypes = {TareaApiController.class})
public class ApiErrorHandler {

    /**
     * Captura excepciones de validación en peticiones REST (MethodArgumentNotValidException).
     *
     * @param ex Excepción capturada
     * @return Mapa asociativo con el nombre del campo y el mensaje de error correspondiente
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(errores);
    }
}
