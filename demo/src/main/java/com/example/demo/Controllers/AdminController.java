package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Modelos.Entity.Cliente;
import com.example.demo.Modelos.Entity.Login;
import com.example.demo.Modelos.Service.ClienteService;
import com.example.demo.Modelos.Service.LoginService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/usuarios")
    public String usuarios(
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.esSuperAdmin()) {
            return "redirect:/";
        }

        model.addAttribute(
                "titulo",
                "Gestión de usuarios");

        model.addAttribute(
                "usuarios",
                loginService.listarUsuarios());

        model.addAttribute(
                "admin",
                true);

        model.addAttribute(
                "adminId",
                login.getId());

        return "usuarios_pendientes";
    }

    @PostMapping("/activar")
    public String activar(
            @RequestParam Long id,
            @RequestParam String rol,
            HttpSession session) {

        Login admin = (Login) session.getAttribute("login");

        if (admin == null) {
            return "redirect:/login/";
        }

        if (!admin.esSuperAdmin()) {
            return "redirect:/";
        }

        if (!rol.equals("ADMIN") && !rol.equals("CLIENTE")) {
            return "redirect:/admin/usuarios";
        }

        loginService.activarUsuario(id, rol);

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/modificar-rol")
    public String modificarRol(
            @RequestParam Long id,
            @RequestParam String rol,
            HttpSession session) {

        Login admin = (Login) session.getAttribute("login");

        if (admin == null) {
            return "redirect:/login/";
        }

        if (!admin.esSuperAdmin()) {
            return "redirect:/";
        }

        // No puede cambiar su propio rol
        if (admin.getId().equals(id)) {
            return "redirect:/admin/usuarios";
        }

        if (!rol.equals("ADMIN") && !rol.equals("CLIENTE")) {
            return "redirect:/admin/usuarios";
        }

        loginService.cambiarRol(id, rol);

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/password/aprobar")
    public String aprobarPassword(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Login admin = (Login) session.getAttribute("login");

        if (admin == null) {
            return "redirect:/login/";
        }

        if (!admin.esSuperAdmin()) {
            return "redirect:/";
        }

        loginService.aprobarCambioPassword(id);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Contraseña actualizada. La persona ya puede entrar con la nueva.");

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/password/rechazar")
    public String rechazarPassword(
            @RequestParam Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Login admin = (Login) session.getAttribute("login");

        if (admin == null) {
            return "redirect:/login/";
        }

        if (!admin.esSuperAdmin()) {
            return "redirect:/";
        }

        loginService.rechazarCambioPassword(id);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Solicitud rechazada. La contraseña anterior sigue vigente.");

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/crear-cliente")
    public String crearCliente(
            Model model,
            HttpSession session) {

        Login admin = (Login) session.getAttribute("login");

        if (admin == null) {
            return "redirect:/login/";
        }

        if (!admin.esSuperAdmin()) {
            return "redirect:/";
        }

        model.addAttribute(
                "titulo",
                "Crear usuario");

        return "crear_cliente";
    }

    @PostMapping("/crear-cliente")
    public String guardarCliente(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String rol,
            Model model,
            HttpSession session) {

        Login admin = (Login) session.getAttribute("login");

        if (admin == null) {
            return "redirect:/login/";
        }

        if (!admin.esSuperAdmin()) {
            return "redirect:/";
        }

        // Validar rol
        if (!rol.equals("ADMIN") && !rol.equals("CLIENTE")) {

            model.addAttribute(
                    "titulo",
                    "Crear usuario");

            model.addAttribute(
                    "error",
                    "El rol seleccionado no es válido.");

            return "crear_cliente";
        }

        // Verificar correo único
        if (loginService.buscarPorEmail(email) != null) {

            model.addAttribute(
                    "titulo",
                    "Crear usuario");

            model.addAttribute(
                    "error",
                    "El correo ya está registrado.");

            return "crear_cliente";
        }

        // Crear Login
        Login nuevoLogin = new Login();

        nuevoLogin.setEmail(email);
        nuevoLogin.setPassword(password);
        nuevoLogin.setEstado("ACTIVO");
        nuevoLogin.setRol(rol);

        nuevoLogin.setCliente(null);

        loginService.guardar(nuevoLogin);

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/perfil")
    public String perfil(
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.esAdmin()) {
            return "redirect:/";
        }

        if (login.getCliente() == null) {
            return "redirect:/admin/perfil/editar";
        }

        // Recuperar el registro actualizado desde la base de datos
        Cliente cliente = clienteService.encontrarCliente(
                login.getCliente().getId());

        if (cliente == null) {
            return "redirect:/admin/perfil/editar";
        }

        // Sincronizar el objeto de sesión
        login.setCliente(cliente);
        session.setAttribute("login", login);

        model.addAttribute("cliente", cliente);
        model.addAttribute("titulo", "Mi perfil");

        return "perfil_admin";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfil(
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.esAdmin()) {
            return "redirect:/";
        }

        Cliente cliente;

        if (login.getCliente() == null) {
            cliente = new Cliente();
            cliente.setEmail(login.getEmail());
        } else {
            // Recuperar el registro existente, incluida su fecha
            cliente = clienteService.encontrarCliente(login.getCliente().getId());

            if (cliente == null) {
                cliente = new Cliente();
                cliente.setEmail(login.getEmail());
            }
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("titulo", "Editar mi perfil");
        model.addAttribute("adminPerfil", true);

        return "modificar_clientes";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(
            Cliente cliente,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.esAdmin()) {
            return "redirect:/";
        }

        // El correo oficial pertenece al Login
        cliente.setEmail(login.getEmail());

        if (login.getCliente() != null) {

            // Recuperar el cliente existente para conservar
            // el ID y la fecha de creación original
            Cliente existente = clienteService.encontrarCliente(login.getCliente().getId());

            if (existente == null) {
                return "redirect:/admin/perfil/editar";
            }

            existente.setNombre(cliente.getNombre());
            existente.setApellido(cliente.getApellido());
            existente.setEmail(login.getEmail());

            clienteService.guardar(existente);

            login.setCliente(existente);

        } else {

            // Crear el perfil solamente la primera vez
            cliente.setId(null);
            cliente.setEmail(login.getEmail());

            clienteService.guardar(cliente);

            // Recuperar el registro persistido para obtener
            // su ID y fecha de creación generada
            Login loginActualizado = loginService.buscarPorEmail(login.getEmail());

            if (loginActualizado == null) {
                return "redirect:/admin/perfil/editar";
            }

            loginActualizado.setCliente(cliente);
            loginService.guardar(loginActualizado);

            login = loginActualizado;
        }

        session.setAttribute("login", login);

        return "redirect:/admin/perfil";
    }

    @GetMapping("/cambiar-password")
    public String cambiarPassword(
            Model model,
            HttpSession session) {

        Login login = (Login) session.getAttribute("login");

        if (login == null) {
            return "redirect:/login/";
        }

        if (!login.esAdmin()) {
            return "redirect:/";
        }

        model.addAttribute(
                "titulo",
                "Cambiar contraseña");

        model.addAttribute(
                "adminPassword",
                true);

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

        if (!login.esAdmin()) {
            return "redirect:/";
        }

        model.addAttribute("titulo", "Cambiar contraseña");
        model.addAttribute("adminPassword", true);

        // Campos vacíos
        if (passwordActual.isBlank()
                || nuevaPassword.isBlank()
                || confirmarPassword.isBlank()) {

            model.addAttribute(
                    "error",
                    "Todos los campos son obligatorios.");

            return "cambiar_password";
        }

        // Confirmar nueva contraseña
        if (!nuevaPassword.equals(confirmarPassword)) {

            model.addAttribute(
                    "error",
                    "Las nuevas contraseñas no coinciden.");

            return "cambiar_password";
        }

        // Evitar colocar la misma contraseña
        if (passwordActual.equals(nuevaPassword)) {

            model.addAttribute(
                    "error",
                    "La nueva contraseña debe ser diferente a la actual.");

            return "cambiar_password";
        }

        // Cambiar contraseña
        boolean cambio = loginService.cambiarPassword(login.getId(),passwordActual,nuevaPassword);

        // La contraseña actual no coincide
        if (!cambio) {

            model.addAttribute(
                    "error",
                    "La contraseña actual es incorrecta.");

            return "cambiar_password";
        }

        login.setPassword(nuevaPassword);

        session.setAttribute("login", login);

        return "redirect:/admin/perfil";
    }
}