/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.otg.dto;

import jakarta.validation.constraints.NotBlank;

public class OtgValidateRequest {

    @NotBlank
    public String code;
}
