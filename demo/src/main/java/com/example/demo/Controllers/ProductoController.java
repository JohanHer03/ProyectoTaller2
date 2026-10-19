package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.Modelos.Entity.Login;
import com.example.demo.Modelos.Entity.Producto;
import com.example.demo.Modelos.Service.ProductoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        model.addAttribute("titulo", "Plataforma de productos");

        return "index_productos";
    }

    @GetMapping("/listar")
    public String listar(Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        model.addAttribute("titulo", "Listado de productos");

        var productos = productoService.listarProductos();

        model.addAttribute("productos", productos);

        return "listar_productos";
    }

    @GetMapping("/agregar")
    public String agregar(Producto producto, Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        model.addAttribute("titulo", "Datos del producto");

        return "modificar_productos";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid Producto producto,
            Errors errores,
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        if (errores.hasErrors()) {

            model.addAttribute(
                    "titulo",
                    producto.getId() == null
                            ? "Datos del producto"
                            : "Editar producto");

            return "modificar_productos";
        }

        productoService.guardar(producto);

        return "redirect:/producto/";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, HttpSession session) {
        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("ADMIN")) {
            return "redirect:/";
        }
        Producto producto = productoService.encontrarProducto(id);

        model.addAttribute("producto", producto);
        model.addAttribute("titulo", "Editar producto");

        return "modificar_productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        try {

            productoService.eliminar(id);

        } catch (RuntimeException e) {

            model.addAttribute(
                    "titulo",
                    "Listado de productos");

            model.addAttribute(
                    "error",
                    e.getMessage());

            model.addAttribute(
                    "productos",
                    productoService.listarProductos());

            return "listar_productos";
        }

        return "redirect:/producto/listar";
    }
}
