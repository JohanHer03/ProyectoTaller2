package com.example.demo.Modelos.Service;

import java.util.List;

import com.example.demo.Modelos.Entity.Detalle;
import com.example.demo.Modelos.Entity.Encabezado;

public interface EncabezadoService {

    List<Encabezado> listarEncabezados();

    Encabezado encontrarEncabezado(Long id);

    void realizarCompra(Long clienteId, List<Detalle> detalles);

    List<Detalle> listarDetalles(Long idEncabezado);

    List<Encabezado> listarComprasCliente(Long clienteId);
}