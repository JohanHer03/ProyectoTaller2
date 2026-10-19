package com.example.demo.Modelos.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.Modelos.Entity.Login;

// Garantiza al arrancar que exista un único superadmin ("sa")
@Component
public class SuperAdminInicializador implements CommandLineRunner {

    @Autowired
    private LoginService loginService;

    @Value("${app.superadmin.email}")
    private String email;

    @Value("${app.superadmin.password-inicial}")
    private String passwordInicial;

    @Override
    public void run(String... args) {

        // Solo puede existir un superadmin
        for (Login usuario : loginService.listarUsuarios()) {

            if (usuario.esSuperAdmin() && !email.equals(usuario.getEmail())) {

                usuario.setRol("ADMIN");
                loginService.guardar(usuario);
            }
        }

        Login sa = loginService.buscarPorEmail(email);

        // Crear la cuenta la primera vez
        if (sa == null) {
            sa = new Login(email, passwordInicial, "ACTIVO", "SUPERADMIN");
        }

        sa.setRol("SUPERADMIN");
        sa.setEstado("ACTIVO");

        loginService.guardar(sa);
    }
}
