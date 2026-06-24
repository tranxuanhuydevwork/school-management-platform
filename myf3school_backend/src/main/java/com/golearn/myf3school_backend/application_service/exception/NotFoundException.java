package com.golearn.myf3school_backend.application_service.exception;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
