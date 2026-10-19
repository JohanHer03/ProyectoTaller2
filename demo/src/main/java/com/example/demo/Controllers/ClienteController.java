package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Modelos.Entity.Cliente;
import com.example.demo.Modelos.Entity.Login;
import com.example.demo.Modelos.Service.ClienteService;
import com.example.demo.Modelos.Service.LoginService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("CLIENTE")) {
            return "redirect:/";
        }

        model.addAttribute("titulo", "Plataforma de clientes");

        return "index_clientes";
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

        model.addAttribute(
                "titulo",
                "Listado de clientes");

        var clientes = clienteService.listarClientes();

        model.addAttribute(
                "clientes",
                clientes);

        return "listar_clientes";
    }

    @GetMapping("/agregar")
    public String agregar(
            Cliente cliente,
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("CLIENTE")) {
            return "redirect:/";
        }

        if (login.getCliente() != null) {
            return "redirect:/cliente/perfil";
        }

        cliente.setEmail(login.getEmail());

        model.addAttribute(
                "titulo",
                "Datos del cliente");

        return "modificar_clientes";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid Cliente cliente,
            Errors errores,
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (login.getRol().equals("CLIENTE")) {

            if (errores.hasErrors()) {

                model.addAttribute(
                        "titulo",
                        login.getCliente() == null
                                ? "Datos del cliente"
                                : "Editar mi perfil");

                return "modificar_clientes";
            }

            if (login.getCliente() == null) {

                // El ID debe ser generado por la BD
                cliente.setId(null);

                // El correo siempre pertenece al Login
                cliente.setEmail(login.getEmail());

                clienteService.guardar(cliente);

                // Asociar Cliente con Login
                login.setCliente(cliente);

                loginService.guardar(login);

                return "redirect:/cliente/perfil";
            }

            cliente.setId(login.getCliente().getId());

            // El correo siempre viene del Login
            cliente.setEmail(login.getEmail());

            clienteService.guardar(cliente);

            // Mantener asociación
            login.setCliente(cliente);

            loginService.guardar(login);

            return "redirect:/cliente/perfil";
        }

        if (login.getRol().equals("ADMIN")) {

            if (cliente.getId() == null) {
                return "redirect:/cliente/listar";
            }

            Login usuario = loginService.buscarPorClienteId(cliente.getId());

            if (usuario == null) {
                return "redirect:/cliente/listar";
            }

            cliente.setEmail(usuario.getEmail());

            clienteService.guardar(cliente);

            return "redirect:/cliente/listar";
        }

        return "redirect:/";
    }

    @GetMapping("/editar/{id}")
    public String editar(
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

        Cliente cliente = clienteService.encontrarCliente(id);

        if (cliente == null) {
            return "redirect:/cliente/listar";
        }

        Login usuario = loginService.buscarPorClienteId(id);

        model.addAttribute("cliente", cliente);
        model.addAttribute("titulo", "Editar cliente");
        model.addAttribute("admin", true);
        model.addAttribute("usuario", usuario);

        return "modificar_clientes";
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

            clienteService.eliminar(id);

        } catch (RuntimeException e) {

            model.addAttribute(
                    "titulo",
                    "Listado de clientes");

            model.addAttribute(
                    "error",
                    e.getMessage());

            model.addAttribute(
                    "clientes",
                    clienteService.listarClientes());

            return "listar_clientes";
        }

        return "redirect:/cliente/listar";
    }

    @GetMapping("/perfil")
    public String perfil(
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        String rol = login.getRol();

        // Perfil del cliente
        if ("CLIENTE".equals(rol)) {

            if (login.getCliente() == null) {
                return "redirect:/cliente/agregar";
            }

            Long clienteId = login.getCliente().getId();

            Cliente clienteActualizado = clienteService.encontrarCliente(clienteId);

            if (clienteActualizado == null) {
                return "redirect:/cliente/agregar";
            }

            login.setCliente(clienteActualizado);
            session.setAttribute("login", login);

            model.addAttribute("cliente", clienteActualizado);
            model.addAttribute("titulo", "Mi perfil");

            return "perfil_cliente";
        }

        return "redirect:/";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfil(Model model, HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("CLIENTE")) {
            return "redirect:/";
        }

        if (login.getCliente() == null) {
            return "redirect:/cliente/agregar";
        }

        model.addAttribute("cliente", login.getCliente());
        model.addAttribute("titulo", "Editar mi perfil");

        return "modificar_clientes";
    }

    @GetMapping("/cambiar-password")
    public String cambiarPassword(
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("CLIENTE")) {
            return "redirect:/";
        }

        model.addAttribute(
                "titulo",
                "Cambiar contraseña");

        return "cambiar_password";
    }

    @PostMapping("/cambiar-password")
    public String guardarPassword(
            @RequestParam String passwordActual,
            @RequestParam String nuevaPassword,
            @RequestParam String confirmarPassword,
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.getRol().equals("CLIENTE")) {
            return "redirect:/";
        }

        model.addAttribute(
                "titulo",
                "Cambiar contraseña");

        // Verificar que no estén vacías
        if (passwordActual.isBlank()
                || nuevaPassword.isBlank()
                || confirmarPassword.isBlank()) {

            model.addAttribute(
                    "error",
                    "Todos los campos son obligatorios.");

            return "cambiar_password";
        }

        // Verificar que las nuevas contraseñas coincidan
        if (!nuevaPassword.equals(confirmarPassword)) {

            model.addAttribute(
                    "error",
                    "Las nuevas contraseñas no coinciden.");

            return "cambiar_password";
        }

        // Verificar que la contraseña actual sea correcta
        if (!login.getPassword().equals(passwordActual)) {

            model.addAttribute(
                    "error",
                    "La contraseña actual es incorrecta.");

            return "cambiar_password";
        }

        // Cambiar contraseña
        loginService.cambiarPassword(
                login.getId(),
                passwordActual,
                nuevaPassword);

        login.setPassword(nuevaPassword);

        session.setAttribute("login", login);

        return "redirect:/cliente/perfil";
    }
}