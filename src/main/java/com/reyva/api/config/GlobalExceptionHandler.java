/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.config;

import com.reyva.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapea las excepciones a respuestas JSON consistentes para que la app cliente
 * nunca “crashee”.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // toggle por propiedad (default: true en dev)
    @Value("${app.log.stacktraces:true}")
    private boolean logStacktraces;

    private String trace() {
        return MDC.get("traceId");
    }

    private ResponseEntity<ErrorResponse> body(HttpStatus status, String msg,
            HttpServletRequest req, Map<String, Object> details) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, msg, req, trace(), details));
    }

    // 400 - JSON mal formado o tipo incompatible en el body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> badJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        var details = Map.of("cause",
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getClass().getSimpleName() : ex.getClass().getSimpleName());
        return body(HttpStatus.BAD_REQUEST, "JSON mal formado", req,
                Map.<String, Object>of("cause",
                        ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getClass().getSimpleName() : ex.getClass().getSimpleName()));
    }

    // 400 - Validaciones en @RequestBody (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> details.put(fe.getField(), fe.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validación fallida", req, details);
    }

    // 400 - Validaciones en @PathVariable/@RequestParam (requiere @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> invalidParams(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(v
                -> details.put(v.getPropertyPath().toString(), v.getMessage()));
        return body(HttpStatus.BAD_REQUEST, "Parámetros inválidos", req, details);
    }

    // 400 - Param obligatorio faltante
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        var details = Map.of("param", ex.getParameterName(), "expectedType", ex.getParameterType());
        return body(HttpStatus.BAD_REQUEST, "Parámetro requerido ausente", req,
                Map.<String, Object>of("param", ex.getParameterName(), "expectedType", ex.getParameterType()));
    }

    // 400 - Tipo incompatible en path/param
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        var details = new java.util.HashMap<String, Object>();
        details.put("param", ex.getName());
        details.put("expected", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido");
        details.put("value", ex.getValue());
        return body(HttpStatus.BAD_REQUEST, "Tipo de dato inválido", req, details);
    }

    // 404 - Rutas/recursos (Spring 6.1+/Boot 3.2+)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundResource(NoResourceFoundException ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, "Recurso no encontrado", req, null);
    }

    // 404 - Compatibilidad (proyectos/entornos que aún lanzan NoHandlerFoundException)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, "Recurso no encontrado", req, null);
    }

    // 401 - Seguridad (cuando llega a los controladores; en general lo maneja tu EntryPoint)
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> unauth(InsufficientAuthenticationException ex, HttpServletRequest req) {
        return body(HttpStatus.UNAUTHORIZED, "No autorizado: token inválido o expirado", req, null);
    }

    // 403 - Seguridad (prohibido)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest req) {
        return body(HttpStatus.FORBIDDEN, "Acceso denegado", req, null);
    }

    // 400/401/409 - Oracle SQL comunes, sin filtrar info sensible
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> sql(SQLException ex, HttpServletRequest req) {
        int code = ex.getErrorCode(); // Oracle usa errorCode
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Error de base de datos";

        switch (code) {
            case 1017:
                status = HttpStatus.UNAUTHORIZED;
                msg = "Usuario o contraseña de base inválidos";
                break; // ORA-01017
            case 1:
                status = HttpStatus.CONFLICT;
                msg = "Registro duplicado (violación de UNIQUE)";
                break; // ORA-00001
            case 2291:
                status = HttpStatus.BAD_REQUEST;
                msg = "No existe la referencia (FK)";
                break;             // ORA-02291
            case 2292:
                status = HttpStatus.CONFLICT;
                msg = "No se puede eliminar por registros relacionados";
                break; // ORA-02292
            case 54:
                status = HttpStatus.CONFLICT;
                msg = "Recurso ocupado, intente nuevamente";
                break;      // ORA-00054
            case 28000:
                status = HttpStatus.UNAUTHORIZED;
                msg = "Cuenta bloqueada";
                break;                         // ORA-28000
            default:
                break;
        }

        var details = Map.of("oracleCode", code, "sqlState", ex.getSQLState());
        return body(status, msg, req,
                Map.<String, Object>of("oracleCode", code, "sqlState", ex.getSQLState()));
    }

    // 400 - Errores de acceso a datos de Spring (si usás JDBC Template/JPA en algún punto)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> dataAccess(DataAccessException ex, HttpServletRequest req) {
        var cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getClass().getSimpleName() : ex.getClass().getSimpleName();
        return body(HttpStatus.BAD_REQUEST, "Error de acceso a datos", req, Map.of("cause", cause));
    }

    // 500 - Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest req) {
        String traceId = MDC.get("traceId");
        if (logStacktraces) {
            log.error("Unhandled error [traceId={}, path={} {}]", traceId, req.getMethod(), req.getRequestURI(), ex);
        } else {
            // en prod: sin stacktrace completo
            log.error("Unhandled error [traceId={}, path={} {}]: {}", traceId, req.getMethod(), req.getRequestURI(), ex.getMessage());
        }
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", req, null);
    }
}
