package com.example.appdelicia01.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

// Esta es tu clase POJO (Plain Old Java Object) para representar un usuario.
// Firestore la usará para mapear los datos de la base de datos a objetos Java.
public class User {

    // --- CAMPOS ---
    // Los nombres de las variables DEBEN COINCIDIR EXACTAMENTE
    // con los nombres de los campos en tu base de datos Firestore.
    private String fullName;
    private String email;
    private String role;

    // Este campo es para la fecha de creación, opcional pero recomendado.
    // La anotación @ServerTimestamp le dice a Firestore que lo llene automáticamente al crear.
    @ServerTimestamp
    private Date createdAt;

    // --- CONSTRUCTOR ---
    // Firestore necesita un constructor público y vacío para poder crear instancias de esta clase.
    public User() {
        // Constructor vacío requerido por Firestore.
    }

    // --- GETTERS Y SETTERS ---
    // Firestore también necesita getters y setters públicos para cada campo.

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
