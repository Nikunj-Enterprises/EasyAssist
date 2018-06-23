package com.easyapper.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler
{
    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<String> handle(AuthServiceException e)
    {
        int statusCode = e.getStatusCode() == 0? 500: e.getStatusCode();
        return ResponseEntity.status(statusCode).body(e.getMessage());
    }
}