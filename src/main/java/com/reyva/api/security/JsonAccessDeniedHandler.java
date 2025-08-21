/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reyva.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 *
 * @author HP
 */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException {
        var body = ErrorResponse.of(HttpStatus.FORBIDDEN,
                "Acceso denegado",
                req, MDC.get("traceId"), null);
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType("application/json");
        om.writeValue(res.getOutputStream(), body);
    }
}
