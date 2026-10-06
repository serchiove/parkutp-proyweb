package com.utp.parkutp.exception;
import org.springframework.http.HttpStatus;
public class NegocioException extends RuntimeException {
 private final HttpStatus status;
 public NegocioException(HttpStatus status, String message) { super(message); this.status=status; }
 public HttpStatus getStatus() { return status; }
 public static NegocioException noEncontrado(String message) { return new NegocioException(HttpStatus.NOT_FOUND, message); }
 public static NegocioException conflicto(String message) { return new NegocioException(HttpStatus.CONFLICT, message); }
}
