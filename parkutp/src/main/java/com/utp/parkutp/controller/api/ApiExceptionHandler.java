package com.utp.parkutp.controller.api;
import com.utp.parkutp.exception.NegocioException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Map;
@RestControllerAdvice(basePackages="com.utp.parkutp.controller.api")
public class ApiExceptionHandler {
 @ExceptionHandler(NegocioException.class) public ResponseEntity<?> negocio(NegocioException ex) { return ResponseEntity.status(ex.getStatus()).body(Map.of("mensaje",ex.getMessage())); }
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validacion(MethodArgumentNotValidException ex) { return ResponseEntity.badRequest().body(Map.of("mensaje","Revisa los datos enviados","errores",ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList())); }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> formato(Exception ex) { return ResponseEntity.badRequest().body(Map.of("mensaje","Formato de solicitud inválido")); }
}
