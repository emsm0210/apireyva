/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.otg;

import com.reyva.api.db.DbExecutor;
import com.reyva.api.security.DbCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Duration;

/**
 *
 * @author HP
 */
@Service
public class OtgService {
    private final OtgDao dao;
    private final OtgCodeGenerator gen = new OtgCodeGenerator();
    private final DbCredentials creds;

    private final int length;
    private final Duration ttl;

    public OtgService(OtgDao dao,
                      DbCredentials creds,
                      @Value("${app.otp.length:6}") int length,
                      @Value("${app.otp.ttl-seconds:300}") int ttlSeconds) {
        this.dao = dao;
        this.creds = creds;
        this.length = length;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public GeneratedOtg generateForCurrentUser() throws SQLException {
        String user = creds.getUser(); // usuario Oracle del JWE
        if (user == null) throw new IllegalStateException("No hay usuario Oracle en el contexto");
        int tries = 0;
        while (true) {
            String code = gen.generate(length);
            try {
                dao.createUnique(code, user, ttl);
                GeneratedOtg out = new GeneratedOtg();
                out.code = code;
                out.expiresInSeconds = (int) ttl.getSeconds();
                out.createdBy = user;
                return out;
            } catch (SQLException e) {
                // ORA-00001: PK (colisión de código) -> reintentar
                if (e.getErrorCode() == 1 && tries++ < 3) continue;
                throw e;
            }
        }
    }

    public ValidationResult validateAndConsume(String code) throws SQLException {
        var recordOgt = dao.findByCode(code);
        if (recordOgt.isEmpty()) return ValidationResult.invalid("Token inexistente");

        var r = recordOgt.get();
        if (!"PENDIENTE".equals(r.status)) return ValidationResult.invalid("Token ya utilizado");
        if (r.expiresAt.toInstant().isBefore(java.time.Instant.now()))
            return ValidationResult.invalid("Token expirado");

        String validator = creds.getUser(); // usuario Oracle del validador
        boolean ok = dao.consumeIfValid(code, validator);
        if (!ok) return ValidationResult.invalid("Token inválido o expirado");

        return ValidationResult.valid(r.createdBy, r.createdAt.toInstant(), validator);
    }

    // DTOs
    public static class GeneratedOtg {
        public String code;
        public int expiresInSeconds;
        public String createdBy;
    }

    public static class ValidationResult {
        public boolean valid;
        public String message;
        public String createdBy;
        public java.time.Instant createdAt;
        public String validatedBy;
        public java.time.Instant validatedAt;

        public static ValidationResult valid(String createdBy, java.time.Instant createdAt, String validatedBy) {
            var vr = new ValidationResult();
            vr.valid = true;
            vr.createdBy = createdBy;
            vr.createdAt = createdAt;
            vr.validatedBy = validatedBy;
            vr.validatedAt = java.time.Instant.now();
            return vr;
        }
        public static ValidationResult invalid(String msg) {
            var vr = new ValidationResult();
            vr.valid = false;
            vr.message = msg;
            return vr;
        }
    }
}