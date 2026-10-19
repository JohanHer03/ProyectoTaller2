package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Modelos.Entity.Login;

import jakarta.servlet.http.HttpSession;

@Controller
public class indexController {

    @GetMapping("/")
    public String inicio(HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (login.getRol().equals("ADMIN")) {
            return "index";
        }

        if (login.getRol().equals("CLIENTE")) {
            return "redirect:/cliente/";
        }

        return "redirect:/login/";
    }
}