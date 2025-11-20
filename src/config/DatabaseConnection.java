/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;
import exceptions.DatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/propiedades_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Opcional: timeouts (ms) para evitar cuelgues largos en conexiones
    private static final int CONNECT_TIMEOUT_MS = 5_000; // 5s
    private static final int SOCKET_TIMEOUT_MS  = 10_000; // 10s

    static {
        try {
            // Carga explícita del driver (útil en algunos runtimes)
            Class.forName("org.mariadb.jdbc.Driver");

            // Valida configuración tempranamente (fail-fast)
            validateConfiguration();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error: No se encontró el driver JDBC de MariaDB/MySQL: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new ExceptionInInitializerError("Error en la configuración de la base de datos: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public static void assertHealthy() throws DatabaseException {
        try (Connection c = getConnection()) {
        } catch (SQLException e) {
            throw new DatabaseException("La base de datos no está disponible: " + e.getMessage(), e);
        }
    }
    
    private static void validateConfiguration() {
        if (URL == null || URL.trim().isEmpty()) {
            throw new IllegalStateException("La URL de la base de datos no está configurada");
        }
        if (USER == null || USER.trim().isEmpty()) {
            throw new IllegalStateException("El usuario de la base de datos no está configurado");
        }
        if (PASSWORD == null) {
            throw new IllegalStateException("La contraseña de la base de datos no está configurada");
        }
    }
}
