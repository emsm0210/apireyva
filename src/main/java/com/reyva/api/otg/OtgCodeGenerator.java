/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.otg;

import java.security.SecureRandom;

/**
 *
 * @author HP
 */
public class OtgCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final SecureRandom rnd = new SecureRandom();

    public String generate(int len) {
        char[] out = new char[len];
        for (int i = 0; i < len; i++) {
            out[i] = ALPHABET[rnd.nextInt(ALPHABET.length)];
        }
        return new String(out);
    }
}
