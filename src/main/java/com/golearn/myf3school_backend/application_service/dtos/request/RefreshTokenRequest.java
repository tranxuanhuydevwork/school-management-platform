package com.golearn.myf3school_backend.application_service.dtos.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}