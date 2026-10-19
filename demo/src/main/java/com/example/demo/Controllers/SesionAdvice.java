package com.example.demo.Controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.Modelos.Entity.Login;

import jakarta.servlet.http.HttpSession;

// Expone el usuario de la sesión a todas las vistas (barra de navegación)
@ControllerAdvice
public class SesionAdvice {

    @ModelAttribute("usuarioActual")
    public Login usuarioActual(HttpSession session) {
        return (Login) session.getAttribute("login");
    }
}
