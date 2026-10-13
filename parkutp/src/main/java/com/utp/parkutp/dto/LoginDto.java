package com.utp.parkutp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginDto {
    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 60)
    private String username;

    // BCrypt solo usa los primeros 72 bytes.
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 72)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}