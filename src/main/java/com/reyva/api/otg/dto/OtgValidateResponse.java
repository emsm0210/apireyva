/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.otg.dto;

/**
 *
 * @author HP
 */
public class OtgValidateResponse {
    public boolean valid;
    public String message;
    public String createdBy;
    public String createdAt;   // ISO-8601 string
    public String validatedBy;
    public String validatedAt;
}
