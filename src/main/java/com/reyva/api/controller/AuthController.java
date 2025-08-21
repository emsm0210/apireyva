/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.controller;

/**
 *
 * @author HP
 */
import com.nimbusds.jose.JOSEException;
import com.reyva.api.dto.AuthRequest;
import com.reyva.api.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import com.reyva.api.dto.ErrorResponse;
import com.reyva.api.security.JweUtil;
import com.reyva.api.service.OracleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OracleAuthService oracleAuthService;
    private final JweUtil jweUtil;

    public AuthController(OracleAuthService oracleAuthService, JweUtil jweUtil) {
        this.oracleAuthService = oracleAuthService;
        this.jweUtil = jweUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        boolean ok = oracleAuthService.authenticate(req.getUsername(), req.getPassword());
        if (!ok) {
            var body = ErrorResponse.of(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        try {
            String token = jweUtil.generateToken(req.getUsername(), req.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (JOSEException e) {
            return ResponseEntity.internalServerError().body("Error generando token");
        }
    }
}
