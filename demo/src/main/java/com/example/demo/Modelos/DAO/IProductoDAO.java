package com.example.demo.Modelos.DAO;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.Modelos.Entity.Producto;

public interface IProductoDAO extends CrudRepository<Producto, Long>{
    
}
