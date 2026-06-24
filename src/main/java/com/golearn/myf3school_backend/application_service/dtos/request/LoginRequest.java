package com.golearn.myf3school_backend.application_service.dtos.request;
import lombok.Data;

@Data
public class LoginRequest {
    private String phoneNumber;
    private String password;
}