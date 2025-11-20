/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import dao.VistasDao;
import entities.Propiedad;

import exceptions.AppException;
import exceptions.ValidationException;
import exceptions.SqlErrorClassifier;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class VistasService {
    private final VistasDao dao = new VistasDao();

    public List<Propiedad> propiedadesCompletas() throws AppException {
        try {
            return dao.listarPropiedadesCompletas();
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    public List<Propiedad> buscarAvanzado(String texto, String rangoSup, String rangoAnt, Integer anio, Integer mes)
            throws AppException {
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new ValidationException("El mes debe estar entre 1 y 12.");
        }

        try {
            return dao.buscarEnVistaAvanzada(texto, rangoSup, rangoAnt, anio, mes);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }
}
