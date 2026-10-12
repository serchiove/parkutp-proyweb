package com.utp.parkutp.model;

public record TokenRespuesta(String token, String tipo, long expiraEnSegundos, String usuario, String rol) {
}