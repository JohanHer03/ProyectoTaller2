package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Modelos.Entity.Login;
import com.example.demo.Modelos.Service.LoginService;

import jakarta.servlet.http.HttpSession;

@Controller
public class indexController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (login.esAdmin()) {

            model.addAttribute("superadmin", login.esSuperAdmin());

            if (login.esSuperAdmin()) {

                // Aviso en el panel de los cambios de contraseña por aprobar
                long solicitudes = loginService.listarUsuarios().stream()
                        .filter(Login::tieneSolicitudPassword)
                        .count();

                model.addAttribute("solicitudesPassword", solicitudes);
            }

            return "index";
        }

        if (login.getRol().equals("CLIENTE")) {
            return "redirect:/cliente/";
        }

        return "redirect:/login/";
    }
}
