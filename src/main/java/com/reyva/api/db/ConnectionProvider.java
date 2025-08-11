/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.db;

import com.reyva.api.security.DbCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author HP
 */
@Service
public class ConnectionProvider {

    private final String url;
    private final DbCredentials dbCredentials;

    public ConnectionProvider(@Value("${spring.datasource.url}") String url,
            DbCredentials dbCredentials) {
        this.url = url;
        this.dbCredentials = dbCredentials;
    }

    public Connection getConnection() throws SQLException {
        if (!dbCredentials.isPresent()) {
            throw new SQLException("No hay credenciales de BD en el contexto de la petición");
        }
        return DriverManager.getConnection(url, dbCredentials.getUser(), dbCredentials.getPass());
    }
}
