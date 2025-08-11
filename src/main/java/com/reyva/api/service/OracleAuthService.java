/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author HP
 */
@Service
public class OracleAuthService {

    private final String url;

    public OracleAuthService(@Value("${spring.datasource.url}") String url) {
        this.url = url;
    }

    public boolean authenticate(String user, String pass) {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            return true; // si conecta, credenciales válidas
        } catch (Exception e) {
            return false;
        }
    }
}
