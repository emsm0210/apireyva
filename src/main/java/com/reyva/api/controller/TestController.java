/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.controller;

import com.reyva.api.db.ConnectionProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.util.Map;

/**
 *
 * @author HP
 */
@RestController
public class TestController {

    private final ConnectionProvider cp;

    public TestController(ConnectionProvider cp) {
        this.cp = cp;
    }

    @GetMapping("/test/secure")
    public Map<String, String> secure() throws Exception {
        try ( var conn = cp.getConnection();  var ps = conn.prepareStatement(
                "select user as CURRENT_USER, sys_context('USERENV','SESSION_USER') as SESSION_USER from dual"); ResultSet rs = ps.executeQuery()) {

            rs.next();
            String currentUser = rs.getString("CURRENT_USER");
            String sessionUser = rs.getString("SESSION_USER");
            return Map.of("status", "ok", "currentUser", currentUser, "sessionUser", sessionUser);
        }
    }
}
