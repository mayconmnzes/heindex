package br.com.heimdex.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("path", request.getRequestURI());
        
        // Isso vai imprimir o erro detalhado no seu terminal do VS Code
        System.err.println("❌ ERRO NA REQUISIÇÃO: " + request.getRequestURI());
        System.err.println("💡 CAUSA: " + ex.getMessage());
        ex.printStackTrace(); 

        return ResponseEntity.internalServerError().body(body);
    }
}