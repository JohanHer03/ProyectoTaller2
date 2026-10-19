package com.example.demo.Modelos.Service;

import java.util.List;

import com.example.demo.Modelos.Entity.Cliente;

public interface ClienteService {
    
    public List<Cliente> listarClientes();

    public void guardar(Cliente cliente);

    public Cliente encontrarCliente(Long id);

    public void eliminar(Long id);
}
