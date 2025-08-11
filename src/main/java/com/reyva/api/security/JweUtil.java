/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.EncryptedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author HP
 */
@Component
public class JweUtil {

    private final byte[] secret;
    private final long expirationMs;

    public JweUtil(@Value("${jwt.enc.secret}") String base64Secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secret = Base64.getDecoder().decode(base64Secret);
        if (secret.length < 32) {
            throw new IllegalArgumentException("JWT_ENC_SECRET debe ser al menos 32 bytes (256 bits) en base64");
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(String dbUser, String dbPass) throws JOSEException {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(dbUser) // para búsquedas rápidas
                .claim("pwd", dbPass) // irá cifrada dentro del JWE
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .build();

        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .type(JOSEObjectType.JWT)
                .build();

        EncryptedJWT jwe = new EncryptedJWT(header, claims);
        SecretKeySpec key = new SecretKeySpec(secret, "AES");
        jwe.encrypt(new DirectEncrypter(key));
        return jwe.serialize();
    }

    public DecodedCreds decode(String token) throws ParseException, JOSEException {
        EncryptedJWT jwe = EncryptedJWT.parse(token);
        SecretKeySpec key = new SecretKeySpec(secret, "AES");
        jwe.decrypt(new DirectDecrypter(key));
        var c = jwe.getJWTClaimsSet();
        return new DecodedCreds(c.getSubject(), (String) c.getClaim("pwd"));
    }

    public static class DecodedCreds {

        public final String user;
        public final String pass;

        public DecodedCreds(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }
    }
}
