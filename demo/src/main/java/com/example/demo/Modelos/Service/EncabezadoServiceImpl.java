package com.example.demo.Modelos.Service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Modelos.DAO.IClienteDAO;
import com.example.demo.Modelos.DAO.IDetalleDAO;
import com.example.demo.Modelos.DAO.IEncabezadoDAO;
import com.example.demo.Modelos.DAO.IProductoDAO;
import com.example.demo.Modelos.Entity.Cliente;
import com.example.demo.Modelos.Entity.Detalle;
import com.example.demo.Modelos.Entity.Encabezado;
import com.example.demo.Modelos.Entity.Producto;

@Service
public class EncabezadoServiceImpl implements EncabezadoService {

    @Autowired
    private IEncabezadoDAO encabezadoDAO;

    @Autowired
    private IDetalleDAO detalleDAO;

    @Autowired
    private IProductoDAO productoDAO;

    @Autowired
    private IClienteDAO clienteDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Encabezado> listarEncabezados() {
        return (List<Encabezado>) encabezadoDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Encabezado encontrarEncabezado(Long id) {
        return encabezadoDAO.findById(id).orElse(null);
    }

    @Transactional
    public Encabezado realizarCompra(Long clienteId, List<Detalle> detalles) {

        double total = 0;

        // 1. Validar productos y stock
        for (Detalle detalle : detalles) {

            Producto producto = productoDAO.findById(detalle.getProducto().getId()).orElseThrow();

            if (detalle.getCantidad() <= 0) {
                throw new RuntimeException(
                    "La cantidad debe ser mayor a cero");
            }

            if (detalle.getCantidad() > producto.getStock()) {
                throw new RuntimeException(
                    "No hay suficiente stock para: "
                    + producto.getNombre());
            }

            double subtotal =
                    producto.getPrecio() * detalle.getCantidad();

            detalle.setValor(subtotal);

            total += subtotal;
        }

        Cliente cliente = clienteDAO.findById(clienteId).orElseThrow();

        Encabezado encabezado = new Encabezado();

        encabezado.setCliente(cliente);
        encabezado.setFecha(new Date());
        encabezado.setTotal(total);

        // Guardar encabezado
        encabezadoDAO.save(encabezado);

        // 2. Guardar detalles y descontar stock
        for (Detalle detalle : detalles) {

            Producto producto = productoDAO.findById(detalle.getProducto().getId()).orElseThrow();

            detalle.setEncabezado(encabezado);
            detalle.setProducto(producto);

            detalleDAO.save(detalle);

            producto.setStock(
                producto.getStock() - detalle.getCantidad()
            );

            productoDAO.save(producto);
        }

        return encabezado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Detalle> listarDetalles(Long idEncabezado) {

        return detalleDAO.findByEncabezadoId(idEncabezado);

    }

    @Transactional(readOnly = true)
    @Override
    public List<Encabezado> listarComprasCliente(Long clienteId) {
        return encabezadoDAO.findByClienteId(clienteId);
    }
}