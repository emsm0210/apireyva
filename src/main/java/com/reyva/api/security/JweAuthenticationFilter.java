/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.security;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

/**
 *
 * @author HP
 */
public class JweAuthenticationFilter extends OncePerRequestFilter {

    private final JweUtil jweUtil;
    private final DbCredentials dbCredentials;

    public JweAuthenticationFilter(JweUtil jweUtil, DbCredentials dbCredentials) {
        this.jweUtil = jweUtil;
        this.dbCredentials = dbCredentials;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Endpoints públicos; añade más si hace falta
        return path.startsWith("/auth/login") || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var creds = jweUtil.decode(token);
                dbCredentials.set(creds.user, creds.pass);

                // Marca al request como autenticado (sin roles por ahora)
                var authentication = new UsernamePasswordAuthenticationToken(
                        creds.user, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ParseException | JOSEException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token inválido o expirado");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
