package com.example.demo.Modelos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Modelos.DAO.IDetalleDAO;
import com.example.demo.Modelos.DAO.IProductoDAO;
import com.example.demo.Modelos.Entity.Producto;

@Service 
public class ProductoServiceImpl implements ProductoService{

    @Autowired 
    private IProductoDAO productoDAO;

    @Autowired
    private IDetalleDAO detalleDAO;

    @Override
    @Transactional (readOnly = true)
    public List<Producto> listarProductos() {
        return (List<Producto>) productoDAO.findAll();
    }

    @Override
    @Transactional
    public void guardar(Producto producto) {
        productoDAO.save(producto);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        if (detalleDAO.existsByProductoId(id)) {
            throw new RuntimeException(
                "No se puede eliminar el producto porque tiene ventas registradas."
            );
        }

        productoDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto encontrarProducto(Long id) {
        return productoDAO.findById(id).orElse(null);
    }
}
