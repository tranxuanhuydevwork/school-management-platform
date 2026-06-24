package com.golearn.myf3school_backend.application_service.dtos.response;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class ApiResponse<T> {
    private boolean success;
    private int status;
    private String message;
    private T data;
    @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).status(200).message("Success").data(data).build();
    }
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().success(true).status(200).message(message).data(data).build();
    }
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder().success(true).status(201).message("Created successfully").data(data).build();
    }
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder().success(false).status(status).message(message).build();
    }
}
