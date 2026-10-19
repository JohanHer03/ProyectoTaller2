package com.example.demo.Modelos.Service;

import java.util.List;

import com.example.demo.Modelos.Entity.Login;

public interface LoginService {

    Login guardar(Login login);

    Login buscarPorEmail(String email);

    Login validarLogin(String email, String password);

    List<Login> listarUsuarios();

    void activarUsuario(Long id, String rol);

    void cambiarRol(Long id, String rol);

    Login buscarPorClienteId(Long clienteId);

    boolean cambiarPassword(
            Long loginId,
            String passwordActual,
            String nuevaPassword
    );

    void solicitarCambioPassword(String email, String nuevaPassword);

    void aprobarCambioPassword(Long id);

    void rechazarCambioPassword(Long id);
}