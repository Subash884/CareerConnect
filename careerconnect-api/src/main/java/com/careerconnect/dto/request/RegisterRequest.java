package com.careerconnect.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String role; // The frontend will send "ROLE_SEEKER" or "ROLE_EMPLOYER"
}