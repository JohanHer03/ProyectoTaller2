package com.example.demo.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Modelos.Entity.Detalle;
import com.example.demo.Modelos.Entity.Encabezado;
import com.example.demo.Modelos.Entity.Login;
import com.example.demo.Modelos.Entity.Producto;
import com.example.demo.Modelos.Service.EncabezadoService;
import com.example.demo.Modelos.Service.FacturaPdfService;
import com.example.demo.Modelos.Service.ProductoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/encabezado")
public class EncabezadoController {

        @Autowired
        private EncabezadoService encabezadoService;

        @Autowired
        private ProductoService productoService;

        @Autowired
        private FacturaPdfService facturaPdfService;

        @GetMapping("/")
        public String inicio(Model model, HttpSession session) {

                Login login = (Login) session.getAttribute("login");

                if (login == null) {
                        return "redirect:/login/";
                }

                if (!login.getRol().equals("ADMIN")) {
                        return "redirect:/";
                }

                model.addAttribute(
                                "titulo",
                                "Compras");

                model.addAttribute(
                                "encabezados",
                                encabezadoService.listarEncabezados());

                return "listar_encabezado";
        }

        @GetMapping("/comprar")
        public String mostrarCompra(
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Login login = (Login) session.getAttribute("login");

                if (login == null) {
                        return "redirect:/login/";
                }

                if (!login.getRol().equals("CLIENTE")) {
                        return "redirect:/";
                }

                if (login.getCliente() == null) {

                        redirectAttributes.addFlashAttribute(
                                        "error",
                                        "Debe completar sus datos personales.");

                        return "redirect:/cliente/agregar";
                }

                model.addAttribute(
                                "productos",
                                productoService.listarProductos());

                return "compra";
        }

        @PostMapping("/comprar")
        public String comprar(
                        @RequestParam Map<String, String> parametros,
                        RedirectAttributes redirectAttributes,
                        HttpSession session) {

                try {

                        Login login = (Login) session.getAttribute("login");

                        if (login == null) {
                                return "redirect:/login/";
                        }

                        if (!login.getRol().equals("CLIENTE")) {
                                return "redirect:/";
                        }

                        if (login.getCliente() == null) {

                                redirectAttributes.addFlashAttribute(
                                                "error",
                                                "Debe completar sus datos personales.");

                                return "redirect:/cliente/agregar";
                        }

                        Long clienteId = login.getCliente().getId();

                        List<Detalle> detalles = new ArrayList<>();

                        for (String clave : parametros.keySet()) {

                                if (clave.startsWith("cantidad_")) {

                                        Long productoId = Long.parseLong(clave.substring(9));

                                        int cantidad = Integer.parseInt(parametros.get(clave));

                                        if (cantidad > 0) {

                                                Producto producto = new Producto();
                                                producto.setId(productoId);

                                                Detalle detalle = new Detalle();

                                                detalle.setProducto(producto);
                                                detalle.setCantidad(cantidad);

                                                detalles.add(detalle);
                                        }
                                }
                        }

                        if (detalles.isEmpty()) {

                                redirectAttributes.addFlashAttribute(
                                                "error",
                                                "Debe seleccionar al menos un producto.");

                                return "redirect:/encabezado/comprar";
                        }

                        encabezadoService.realizarCompra(
                                        clienteId,
                                        detalles);

                        return "redirect:/cliente/";

                } catch (RuntimeException e) {

                        redirectAttributes.addFlashAttribute(
                                        "error",
                                        e.getMessage());

                        return "redirect:/encabezado/comprar";
                }
        }

        @GetMapping("/detalles/{id}")
        public String detalles(
                        @PathVariable Long id,
                        Model model,
                        HttpSession session) {

                Login login = (Login) session.getAttribute("login");

                if (login == null) {
                        return "redirect:/login/";
                }

                Encabezado encabezado = encabezadoService.encontrarEncabezado(id);

                if (encabezado == null) {
                        return "redirect:/";
                }

                // ADMIN puede ver cualquier compra
                if (login.getRol().equals("ADMIN")) {

                        model.addAttribute(
                                        "encabezado",
                                        encabezado);

                        model.addAttribute(
                                        "detalles",
                                        encabezadoService.listarDetalles(id));

                        model.addAttribute("admin", true);

                        return "listar_detalles";
                }

                // CLIENTE solamente puede ver sus propias compras
                if (login.getRol().equals("CLIENTE")) {

                        if (login.getCliente() == null) {
                                return "redirect:/cliente/agregar";
                        }

                        Long clienteId = login.getCliente().getId();

                        Long clienteCompraId = encabezado.getCliente().getId();

                        if (!clienteId.equals(clienteCompraId)) {
                                return "redirect:/encabezado/mis-compras";
                        }

                        model.addAttribute(
                                        "encabezado",
                                        encabezado);

                        model.addAttribute(
                                        "detalles",
                                        encabezadoService.listarDetalles(id));

                        model.addAttribute(
                                        "admin",
                                        false);

                        return "listar_detalles";
                }

                return "redirect:/";
        }

        @GetMapping("/mis-compras")
        public String misCompras(
                        Model model,
                        HttpSession session) {

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

                Long clienteId = login.getCliente().getId();

                model.addAttribute(
                                "encabezados",
                                encabezadoService.listarComprasCliente(clienteId));

                model.addAttribute(
                                "titulo",
                                "Mis compras");

                return "mis_compras";
        }

        @GetMapping("/factura/{id}/pdf")
        public Object descargarFactura(
                        @PathVariable Long id,
                        HttpSession session,
                        HttpServletResponse response) {

                Login login = (Login) session.getAttribute("login");

                if (login == null) {
                        return ResponseEntity.status(401).build();
                }

                if (!"ADMIN".equals(login.getRol()) && !"CLIENTE".equals(login.getRol())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return "error/403";
                }

                Encabezado encabezado = encabezadoService.encontrarEncabezado(id);

                if (encabezado == null) {
                        return ResponseEntity.notFound().build();
                }

                // El cliente solo puede descargar sus propias facturas.
                if ("CLIENTE".equals(login.getRol())) {

                        if (login.getCliente() == null) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                return "error/403";
                        }

                        Long clienteId = login.getCliente().getId();
                        Long clienteCompraId = encabezado.getCliente().getId();

                        if (!clienteId.equals(clienteCompraId)) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                return "error/403";
                        }
                }

                try {
                        byte[] pdf = facturaPdfService.generarFactura(
                                        encabezado,
                                        encabezadoService.listarDetalles(id));

                        return ResponseEntity.ok()
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"factura-" + id + ".pdf\"")
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .contentLength(pdf.length)
                                        .body(pdf);

                } catch (java.io.IOException e) {
                        return ResponseEntity.internalServerError().build();
                }
        }

}