/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.security;

import java.util.Base64;

/**
 *
 * @author HP
 */
public class CryptoUtil {

    private static final String SECRET_KEY = "u7yzNDf02KoqGlvujCmBbzF81UTuX/0qE7gSTZ9qVgU="; // cámbiala por algo seguro

    public static String encrypt(String plainText) {
        byte[] keyBytes = SECRET_KEY.getBytes();
        byte[] textBytes = plainText.getBytes();
        byte[] encrypted = new byte[textBytes.length];

        for (int i = 0; i < textBytes.length; i++) {
            encrypted[i] = (byte) (textBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedText) {
        byte[] keyBytes = SECRET_KEY.getBytes();
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = new byte[encryptedBytes.length];

        for (int i = 0; i < encryptedBytes.length; i++) {
            decrypted[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return new String(decrypted);
    }
}
