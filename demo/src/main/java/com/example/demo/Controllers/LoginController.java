package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Modelos.Entity.Login;
import com.example.demo.Modelos.Service.LoginService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String login() {

        return "login";
    }

    @PostMapping("/ingresar")
    public String ingresar(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Login login = loginService.validarLogin(email, password);

        if (login == null) {

            model.addAttribute(
                    "error",
                    "Correo o contraseña incorrectos.");

            return "login";
        }

        if (!login.getEstado().equals("ACTIVO")) {

            model.addAttribute(
                    "error",
                    "La cuenta aún no ha sido activada por un administrador.");

            return "login";
        }

        session.setAttribute("login", login);

        if (login.getRol().equals("ADMIN")) {
            if (login.getCliente() == null) {

                return "redirect:/admin/perfil";
            }
            return "redirect:/";

        }

        if (login.getRol().equals("CLIENTE")) {

            if (login.getCliente() == null) {

                return "redirect:/cliente/agregar";
            }

            return "redirect:/cliente/";
        }

        model.addAttribute(
                "error",
                "El usuario no tiene un rol válido.");

        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {

        model.addAttribute("login", new Login());

        return "registro";
    }

    @PostMapping("/registrar")
    public String registrar(
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        Login existente = loginService.buscarPorEmail(email);

        if (existente != null) {

            model.addAttribute(
                    "error",
                    "El correo ya está registrado.");

            return "registro";
        }

        Login login = new Login();

        login.setEmail(email);
        login.setPassword(password);
        login.setEstado("PENDIENTE");
        login.setRol("PENDIENTE");

        loginService.guardar(login);

        model.addAttribute(
                "mensaje",
                "Registro realizado. Debe esperar la aprobación de un administrador.");

        return "login";
    }

    @GetMapping("/salir")
    public String salir(HttpSession session) {

        session.invalidate();

        return "redirect:/login/";
    }
}