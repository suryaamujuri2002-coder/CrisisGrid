package com.project.CrisisGrid.crisis_service.security;



import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}