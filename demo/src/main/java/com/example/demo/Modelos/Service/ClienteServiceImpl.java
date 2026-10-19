package com.example.demo.Modelos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Modelos.DAO.IClienteDAO;
import com.example.demo.Modelos.DAO.IEncabezadoDAO;
import com.example.demo.Modelos.DAO.ILoginDAO;
import com.example.demo.Modelos.Entity.Cliente;
import com.example.demo.Modelos.Entity.Login;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private IClienteDAO clienteDAO;

    @Autowired
    private IEncabezadoDAO encabezadoDAO;

    @Autowired
    private ILoginDAO loginDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return (List<Cliente>) clienteDAO.findAll();
    }

    @Override
    @Transactional
    public void guardar(Cliente cliente) {
        clienteDAO.save(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Cliente cliente = clienteDAO.findById(id).orElse(null);

        if (cliente == null) {
            throw new RuntimeException(
                    "El cliente no existe.");
        }

        Login login = loginDAO.findByClienteId(id).orElse(null);

        if (login != null && login.esSuperAdmin()) {

            throw new RuntimeException(
                    "No se puede eliminar al superadministrador.");
        }

        if (encabezadoDAO.existsByClienteId(id)) {

            throw new RuntimeException(
                    "No se puede eliminar el cliente porque tiene compras registradas.");
        }

        if (login != null) {
            loginDAO.delete(login);
        }

        clienteDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente encontrarCliente(Long id) {
        return clienteDAO.findById(id).orElse(null);
    }

}
