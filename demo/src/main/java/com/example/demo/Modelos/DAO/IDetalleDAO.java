package com.example.demo.Modelos.DAO;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.Modelos.Entity.Detalle;

public interface IDetalleDAO extends CrudRepository<Detalle, Long> {
    List<Detalle> findByEncabezadoId(Long idEncabezado);

    boolean existsByProductoId(Long productoId);
}