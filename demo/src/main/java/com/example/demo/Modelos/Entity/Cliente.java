
package com.example.demo.Modelos.Entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.persistence.PrePersist;

@Entity
/* @Table(name = "cliente") */
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El campo no puede estar vacio")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "El nombre solo puede contener letras.")
    private String nombre;

    @NotEmpty(message = "El campo no puede estar vacio")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "El apellido solo puede contener letras.")
    private String apellido;

    @NotEmpty(message = "El campo no puede estar vacio")
    @Column(unique = true)
    private String email;

    @Column(updatable = false)
    private Date crDate;

    public Cliente() {

    }

    public Cliente(String nombre, String apellido, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }

    @PrePersist
    public void prePersist() {
        if (this.crDate == null) {
            this.crDate = new Date();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCrDate() {
        return crDate;
    }

    public void setCrDate(Date fecha) {
        this.crDate = fecha;
    }

    @Override
    public String toString() {
        return "Cliente [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido + ", email=" + email + ", crDate="
                + new SimpleDateFormat("dd/MM/yy-HH:mm:ss").format(crDate) + " ]";
    }

}
