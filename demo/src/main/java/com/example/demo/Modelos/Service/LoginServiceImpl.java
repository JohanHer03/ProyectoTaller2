package com.example.demo.Modelos.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Modelos.DAO.ILoginDAO;
import com.example.demo.Modelos.Entity.Login;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private ILoginDAO loginDAO;

    @Override
    @Transactional
    public Login guardar(Login login) {
        return loginDAO.save(login);
    }

    @Override
    @Transactional(readOnly = true)
    public Login buscarPorEmail(String email) {
        return loginDAO.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Login validarLogin(String email, String password) {

        Login login = loginDAO.findByEmail(email).orElse(null);

        if (login == null) {
            return null;
        }

        if (!login.getPassword().equals(password)) {
            return null;
        }

        return login;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Login> listarUsuarios() {
        return (List<Login>) loginDAO.findAll();
    }

    @Override
    @Transactional
    public void activarUsuario(Long id, String rol) {

        Login login = loginDAO.findById(id).orElse(null);

        // El superadmin no puede ser modificado
        if (login != null && !login.esSuperAdmin()) {

            login.setEstado("ACTIVO");
            login.setRol(rol);

            loginDAO.save(login);
        }
    }

    @Override
    @Transactional
    public void cambiarRol(Long id, String rol) {

        Login login = loginDAO.findById(id).orElse(null);

        // El superadmin no puede ser modificado
        if (login != null && !login.esSuperAdmin()) {

            login.setRol(rol);

            loginDAO.save(login);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Login buscarPorClienteId(Long clienteId) {
        return loginDAO.findByClienteId(clienteId).orElse(null);
    }

    @Override
    @Transactional
    public boolean cambiarPassword(
            Long loginId,
            String passwordActual,
            String nuevaPassword) {

        Login login = loginDAO.findById(loginId).orElse(null);

        if (login == null) {
            return false;
        }

        // Verificar contraseña actual
        if (!login.getPassword().equals(passwordActual)) {
            return false;
        }

        // Cambiar contraseña; una solicitud pendiente ya no aplica
        login.setPassword(nuevaPassword);
        login.setPasswordSolicitada(null);

        loginDAO.save(login);

        return true;
    }

    @Override
    @Transactional
    public void solicitarCambioPassword(String email, String nuevaPassword) {

        Login login = loginDAO.findByEmail(email).orElse(null);

        // El superadmin no tiene quién le apruebe la solicitud
        if (login == null || login.esSuperAdmin()) {
            return;
        }

        login.setPasswordSolicitada(nuevaPassword);

        loginDAO.save(login);
    }

    @Override
    @Transactional
    public void aprobarCambioPassword(Long id) {

        Login login = loginDAO.findById(id).orElse(null);

        if (login != null && login.tieneSolicitudPassword()) {

            login.setPassword(login.getPasswordSolicitada());
            login.setPasswordSolicitada(null);

            loginDAO.save(login);
        }
    }

    @Override
    @Transactional
    public void rechazarCambioPassword(Long id) {

        Login login = loginDAO.findById(id).orElse(null);

        if (login != null && login.tieneSolicitudPassword()) {

            login.setPasswordSolicitada(null);

            loginDAO.save(login);
        }
    }
}