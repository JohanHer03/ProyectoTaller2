package com.example.demo.Modelos.Service;

import java.util.List;

import com.example.demo.Modelos.Entity.Producto;

public interface ProductoService {

    public List<Producto> listarProductos();

    public void guardar(Producto producto);

    public void eliminar(Long id);

    public Producto encontrarProducto(Long id);
}
