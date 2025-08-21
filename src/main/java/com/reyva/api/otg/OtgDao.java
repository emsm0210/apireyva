/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.otg;

import com.reyva.api.db.DbExecutor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Duration;
import java.util.Optional;

/**
 *
 * @author HP
 */
@Repository
public class OtgDao {
    private final DbExecutor db;

    public OtgDao(DbExecutor db) { this.db = db; }

    /** Inserta un OTG único. Reintenta si choca con PK. */
    public String createUnique(String code, String createdBy, Duration ttl) throws SQLException {
        return db.withTx(conn -> {
            // EXPIRES_AT = SYSTIMESTAMP + ttl (en segundos)
            String sql = "INSERT INTO OTG_AUTH (CODE, CREATED_AT, EXPIRES_AT, CREATED_BY, STATUS) " +
                         "VALUES (?, SYSDATE, SYSDATE + NUMTODSINTERVAL(?, 'SECOND'), ?, 'PENDIENTE')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, code);
                ps.setInt(2, (int) ttl.getSeconds());
                ps.setString(3, createdBy);
                ps.executeUpdate();
                return code;
            }
        });
    }

    public Optional<OtgRecord> findByCode(String code) throws SQLException {
        return db.readOnly(conn -> {
            String q = "SELECT CODE, CREATED_AT, EXPIRES_AT, CREATED_BY, STATUS, USED_AT, USED_BY " +
                       "FROM OTG_AUTH WHERE CODE = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    OtgRecord r = new OtgRecord();
                    r.code = rs.getString("CODE");
                    r.createdAt = rs.getTimestamp("CREATED_AT");
                    r.expiresAt = rs.getTimestamp("EXPIRES_AT");
                    r.createdBy = rs.getString("CREATED_BY");
                    r.status = rs.getString("STATUS");
                    r.usedAt = rs.getTimestamp("USED_AT");
                    r.usedBy = rs.getString("USED_BY");
                    return Optional.of(r);
                }
            }
        });
    }

    /** Marca como USED si sigue PENDING y no expiró. Devuelve true si se consumió. */
    public boolean consumeIfValid(String code, String validatorUser) throws SQLException {
        return db.withTx(conn -> {
            String upd = "UPDATE OTG_AUTH " +
                         "SET STATUS='USADO', USED_AT=SYSDATE, USED_BY=? " +
                         "WHERE CODE=? AND STATUS='PENDIENTE' AND EXPIRES_AT > SYSDATE";
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setString(1, validatorUser);
                ps.setString(2, code);
                int n = ps.executeUpdate();
                return n > 0;
            }
        });
    }

    // DTO interno
    public static class OtgRecord {
        public String code, createdBy, status, usedBy;
        public Timestamp createdAt, expiresAt, usedAt;
    }
}
