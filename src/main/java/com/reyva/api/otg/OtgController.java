/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.otg;

import com.reyva.api.dto.ErrorResponse;
import com.reyva.api.otg.dto.OtgGenerateResponse;
import com.reyva.api.otg.dto.OtgValidateRequest;
import com.reyva.api.otg.dto.OtgValidateResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author HP
 */
@RestController
@RequestMapping("/otg")
public class OtgController {

    private final OtgService svc;

    public OtgController(OtgService svc) { this.svc = svc; }

    // Generar: lo usa la app Flutter (usuario Oracle del móvil)
    @PostMapping("/generate")
    public ResponseEntity<?> generate() throws Exception {
        var g = svc.generateForCurrentUser();
        var resp = new OtgGenerateResponse();
        resp.code = g.code;
        resp.expiresInSeconds = g.expiresInSeconds;
        resp.createdBy = g.createdBy;
        return ResponseEntity.ok(resp);
    }

    // Validar/consumir: lo usa "el otro sistema" (con su propio usuario Oracle)
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@Valid @RequestBody OtgValidateRequest req, HttpServletRequest httpReq) throws Exception {
        var vr = svc.validateAndConsume(req.code);
        if (!vr.valid) {
            var err = ErrorResponse.of(HttpStatus.BAD_REQUEST, vr.message, httpReq, null, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        var out = new OtgValidateResponse();
        out.valid = true;
        out.createdBy = vr.createdBy;
        out.createdAt = vr.createdAt.toString();
        out.validatedBy = vr.validatedBy;
        out.validatedAt = vr.validatedAt.toString();
        return ResponseEntity.ok(out);
    }
}
