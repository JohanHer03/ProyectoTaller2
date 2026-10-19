package com.example.demo.Modelos.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.Modelos.Entity.Login;

public interface ILoginDAO extends CrudRepository<Login, Long> {

    Optional<Login> findByEmail(String email);
    
    List<Login> findByEstado(String estado);

    Optional<Login> findByClienteId(Long clienteId);
}