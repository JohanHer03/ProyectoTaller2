package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Modelos.Entity.Login;

import jakarta.servlet.http.HttpSession;

@Controller
public class indexController {

    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (login.esAdmin()) {

            model.addAttribute("superadmin", login.esSuperAdmin());

            return "index";
        }

        if (login.getRol().equals("CLIENTE")) {
            return "redirect:/cliente/";
        }

        return "redirect:/login/";
    }
}