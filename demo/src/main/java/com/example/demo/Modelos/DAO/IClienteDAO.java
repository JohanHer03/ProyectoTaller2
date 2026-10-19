package com.example.demo.Modelos.DAO;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.Modelos.Entity.Cliente;

public interface IClienteDAO extends CrudRepository<Cliente, Long>{
    
}
