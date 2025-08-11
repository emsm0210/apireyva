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
import org.springframework.security.web.SecurityFilterChain;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var jweFilter = new JweAuthenticationFilter(jweUtil, dbCredentials);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(req -> {
            CorsConfiguration c = new CorsConfiguration();
            // DEV: abre todo; luego restringe a la URL real de Flutter
            c.setAllowedOrigins(List.of("*"));
            c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            c.setAllowedHeaders(List.of("*"));
            c.setExposedHeaders(List.of("Authorization"));
            c.setAllowCredentials(false);
            return c;
        }))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .anyRequest().authenticated()
                )
                .addFilterBefore(jweFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
