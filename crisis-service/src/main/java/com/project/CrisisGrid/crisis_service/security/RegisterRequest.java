package com.project.CrisisGrid.crisis_service.security;



import lombok.Data;

@Data
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
}