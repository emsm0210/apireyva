/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reyva.api.repository;

import com.reyva.api.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository extends JpaRepository<Persona, String> {

    @Query(value = """
        SELECT P.USUARIO, P.NOMBRES, P.APELLIDOS
        FROM PERSONA P
        WHERE P.USUARIO = :usuario
        """, nativeQuery = true)
    Persona findByUsuario(@Param("usuario") String usuario);
}