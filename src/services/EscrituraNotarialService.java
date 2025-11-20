/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import dao.EscrituraNotarialDao;
import entities.EscrituraNotarial;

import exceptions.AppException;
import exceptions.ValidationException;
import exceptions.NotFoundException;
import exceptions.SqlErrorClassifier;

import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class EscrituraNotarialService implements GenericService<EscrituraNotarial> {

    private final EscrituraNotarialDao escrituraDao;

    public EscrituraNotarialService() {
        this.escrituraDao = new EscrituraNotarialDao();
    }

    @Override
    public void insertar(EscrituraNotarial entidad) throws AppException {
        validarInsert(entidad);
        try {
            escrituraDao.crear(entidad);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public void actualizar(EscrituraNotarial entidad) throws AppException {
        validarUpdate(entidad);
        try {
            escrituraDao.actualizar(entidad);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public void eliminar(Long id) throws AppException {
        if (id == null) throw new ValidationException("Id requerido para eliminar.");
        try {
            escrituraDao.eliminarLogico(id);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public EscrituraNotarial getById(Long id) throws AppException {
        if (id == null) throw new ValidationException("Id requerido.");
        try {
            EscrituraNotarial e = escrituraDao.leerPorId(id);
            if (e == null) throw new NotFoundException("Escritura id=" + id + " no encontrada.");
            return e;
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public List<EscrituraNotarial> getAll() throws AppException {
        try {
            return escrituraDao.leerTodos();
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    // ------------- Métodos extra específicos -------------
    public EscrituraNotarial buscarPorNumero(String nro) throws AppException {
        if (nro == null || nro.isBlank())
            throw new ValidationException("Número de escritura requerido.");
        try {
            return escrituraDao.buscarPorNroEscritura(nro); // puede devolver null
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    public EscrituraNotarial buscarPorPropiedadId(Long propiedadId) throws AppException {
        if (propiedadId == null)
            throw new ValidationException("propiedadId requerido.");
        try {
            return escrituraDao.buscarPorPropiedadId(propiedadId); // puede devolver null
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    // ---------- Validaciones mínimas de negocio ----------
    private void validarInsert(EscrituraNotarial e) throws ValidationException {
        if (e == null) throw new ValidationException("Escritura requerida.");
        if (e.getNroEscritura() == null || e.getNroEscritura().isBlank())
            throw new ValidationException("El número de escritura es obligatorio.");
        if (e.getPropiedadId() == null)
            throw new ValidationException("La escritura debe estar asociada a una propiedad (propiedadId).");
    }

    private void validarUpdate(EscrituraNotarial e) throws ValidationException {
        if (e == null) throw new ValidationException("Escritura requerida.");
        if (e.getId() == null) throw new ValidationException("Id de escritura requerido para actualizar.");
        if (e.getNroEscritura() == null || e.getNroEscritura().isBlank())
            throw new ValidationException("El número de escritura es obligatorio.");
    }
    
}
