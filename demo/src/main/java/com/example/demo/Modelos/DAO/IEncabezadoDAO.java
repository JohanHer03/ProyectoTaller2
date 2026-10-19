package com.example.demo.Modelos.DAO;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.Modelos.Entity.Encabezado;

public interface IEncabezadoDAO extends CrudRepository<Encabezado, Long> {
    List<Encabezado> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}