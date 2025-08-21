/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

/**
 *
 * @author HP
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public Instant timestamp;
    public String traceId;
    public int status;
    public String error;
    public String message;
    public String path;
    public String method;
    public Map<String, Object> details;

    public static ErrorResponse of(HttpStatus status, String message,
                                   HttpServletRequest req, String traceId,
                                   Map<String, Object> details) {
        ErrorResponse r = new ErrorResponse();
        r.timestamp = Instant.now();
        r.traceId = traceId;
        r.status = status.value();
        r.error = status.getReasonPhrase();
        r.message = message;
        if (req != null) {
            r.path = req.getRequestURI();
            r.method = req.getMethod();
        }
        r.details = details;
        return r;
    }
}