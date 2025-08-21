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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 *
 * @author HP
 */
public class JsonAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        var body = ErrorResponse.of(HttpStatus.UNAUTHORIZED,
                "No autorizado: token inválido o expirado",
                req, MDC.get("traceId"), null);
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json");
        om.writeValue(res.getOutputStream(), body);
    }
}
