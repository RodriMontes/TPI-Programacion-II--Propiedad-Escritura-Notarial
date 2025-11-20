/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;
import config.DatabaseConnection;
import dao.PropiedadDao;
import dao.EscrituraNotarialDao;
import entities.EscrituraNotarial;
import entities.Propiedad;

import exceptions.AppException;
import exceptions.ValidationException;
import exceptions.NotFoundException;
import exceptions.SqlErrorClassifier;
import exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class PropiedadService implements GenericService<Propiedad> {

    private final PropiedadDao propiedadDao;
    private final EscrituraNotarialDao escrituraDao;

    public PropiedadService() {
        this.propiedadDao = new PropiedadDao();
        this.escrituraDao = new EscrituraNotarialDao();
    }

    // ======================= CRUD =======================
    @Override
    public void insertar(Propiedad entidad) throws AppException {
        validarInsertUpdate(entidad);
        try {
            propiedadDao.crear(entidad);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public void actualizar(Propiedad entidad) throws AppException {
        validarInsertUpdate(entidad);
        if (entidad.getId() == null)
            throw new ValidationException("Id requerido para actualizar.");
        try {
            propiedadDao.actualizar(entidad);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public void eliminar(Long id) throws AppException {
        if (id == null) throw new ValidationException("Id requerido para eliminar.");
        try {
            propiedadDao.eliminarLogico(id);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public Propiedad getById(Long id) throws AppException {
        if (id == null) throw new ValidationException("Id requerido.");
        try {
            Propiedad p = propiedadDao.leerPorId(id);
            if (p == null) throw new NotFoundException("Propiedad id=" + id + " no encontrada.");
            cargarEscritura(p);
            return p;
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    @Override
    public List<Propiedad> getAll() throws AppException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Propiedad> lista = propiedadDao.leerTodos(conn);
            for (Propiedad p : lista) {
                EscrituraNotarial e = escrituraDao.buscarPorPropiedadId(conn, p.getId());
                p.setEscrituraNotarial(e);
            }
            return lista;
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    // ================= Métodos extra =================
    public Propiedad buscarPorPadron(String padron) throws AppException {
        if (padron == null || padron.isBlank())
            throw new ValidationException("Padrón requerido.");
        try {
            Propiedad p = propiedadDao.buscarPorPadronCatastral(padron);
            if (p != null) cargarEscritura(p);
            return p; // puede ser null si no existe
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    public Propiedad buscarPorIdEscritura(Long idEscritura) throws AppException {
        if (idEscritura == null)
            throw new ValidationException("Id de escritura requerido.");
        try {
            // 1) escritura
            EscrituraNotarial escritura = escrituraDao.leerPorId(idEscritura);
            if (escritura == null) return null;
            Long propiedadId = escritura.getPropiedadId();
            if (propiedadId == null) return null;

            // 2) propiedad asociada
            return propiedadDao.leerPorId(propiedadId);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    // ---- Operación transaccional ----
    public void altaPropiedadConEscrituraTransaccional(Propiedad propiedad, EscrituraNotarial escritura) throws AppException {
        validarInsertUpdate(propiedad);
        if (escritura == null)
            throw new ValidationException("Escritura requerida.");
        Connection conn = null;
        boolean prev = true;
        try {
            conn = DatabaseConnection.getConnection();
            prev = conn.getAutoCommit();
            conn.setAutoCommit(false);

            propiedadDao.crear(conn, propiedad);

            escritura.setPropiedadId(propiedad.getId());
            escrituraDao.crear(conn, escritura);

            conn.commit();
        } catch (SQLException sql) {
            safeRollback(conn);
            throw SqlErrorClassifier.map(sql);
        } catch (Exception ex) {
            safeRollback(conn);
            throw new DatabaseException("Error inesperado en la transacción.", ex);
        } finally {
            safeRestoreAndClose(conn, prev);
        }
    }
    
    // ---------- Validaciones ----------
    private void validarInsertUpdate(Propiedad p) throws ValidationException {
        if (p == null) throw new ValidationException("Propiedad requerida.");
        if (p.getPadronCatastral() == null || p.getPadronCatastral().isBlank())
            throw new ValidationException("El padrón catastral es obligatorio.");
        if (p.getDireccion() == null || p.getDireccion().isBlank())
            throw new ValidationException("La dirección es obligatoria.");
        if (p.getSuperficieM2() == null || p.getSuperficieM2().signum() <= 0)
            throw new ValidationException("La superficie debe ser > 0.");
        if (p.getDestino() == null)
            throw new ValidationException("El destino es obligatorio (RES/COM).");
    }

    // ================= Helpers =================
    private void cargarEscritura(Propiedad p) throws AppException {
        if (p == null) return;
        try {
            var e = escrituraDao.buscarPorPropiedadId(p.getId());
            p.setEscrituraNotarial(e);
        } catch (SQLException sql) {
            throw SqlErrorClassifier.map(sql);
        }
    }

    private static void safeRollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
    }
    private static void safeRestoreAndClose(Connection c, boolean prevAuto) {
        if (c != null) {
            try { c.setAutoCommit(prevAuto); } catch (SQLException ignore) {}
            try { c.close(); } catch (SQLException ignore) {}
        }
    }
}