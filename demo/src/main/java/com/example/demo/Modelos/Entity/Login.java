package com.example.demo.Modelos.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String estado;

    private String rol;

    // Nueva contraseña pedida desde el login; pendiente de aprobación del superadmin
    private String passwordSolicitada;

    @OneToOne
    private Cliente cliente;

    public Login() {
    }

    public Login(String email, String password, String estado, String rol) {
        this.email = email;
        this.password = password;
        this.estado = estado;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getPasswordSolicitada() {
        return passwordSolicitada;
    }

    public void setPasswordSolicitada(String passwordSolicitada) {
        this.passwordSolicitada = passwordSolicitada;
    }

    public boolean tieneSolicitudPassword() {
        return passwordSolicitada != null;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    // El superadmin tiene todos los permisos de un admin
    public boolean esAdmin() {
        return "ADMIN".equals(rol) || "SUPERADMIN".equals(rol);
    }

    public boolean esSuperAdmin() {
        return "SUPERADMIN".equals(rol);
    }
}