/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.db;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 *
 * @author HP
 */
@Service
public class DbExecutor {

    private final ConnectionProvider cp;

    public DbExecutor(ConnectionProvider cp) {
        this.cp = cp;
    }

    public <T> T withTx(SqlFunction<Connection, T> work) throws SQLException {
        try (Connection c = cp.getConnection()) {
            boolean prev = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T result = work.apply(c);
                c.commit();
                return result;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(prev);
            }
        }
    }

    public <T> T readOnly(SqlFunction<Connection, T> work) throws SQLException {
        try (Connection c = cp.getConnection()) {
            return work.apply(c); // autocommit por defecto
        }
    }

    @FunctionalInterface
    public interface SqlFunction<C, R> {

        R apply(C conn) throws SQLException;
    }
}
