/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 *
 * @author HP
 */
@Configuration
public class SecurityConfig {

    private final JweUtil jweUtil;
    private final DbCredentials dbCredentials;

    public SecurityConfig(JweUtil jweUtil, DbCredentials dbCredentials) {
        this.jweUtil = jweUtil;
        this.dbCredentials = dbCredentials;
    }

    @Bean
    AuthenticationEntryPoint jsonAuthEntryPoint() {
        return new JsonAuthEntryPoint();
    }

    @Bean
    AccessDeniedHandler jsonAccessDeniedHandler() {
        return new JsonAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var jweFilter = new JweAuthenticationFilter(jweUtil, dbCredentials, jsonAuthEntryPoint());

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(req -> {
            CorsConfiguration c = new CorsConfiguration();
            c.setAllowedOrigins(List.of("*")); // ajusta a tu Flutter
            c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            c.setAllowedHeaders(List.of("*"));
            c.setExposedHeaders(List.of("Authorization", "X-Request-Id"));
            return c;
        }))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jsonAuthEntryPoint())
                .accessDeniedHandler(jsonAccessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .anyRequest().authenticated())
                .addFilterBefore(new RequestIdFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jweFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
