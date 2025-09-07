package com.example.jsonplaceholderapi.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username ou email é obrigatório")
    private String usernameOrEmail;

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    // Getters and Setters
    public String getUsernameOrEmail() { return usernameOrEmail; }
    public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}