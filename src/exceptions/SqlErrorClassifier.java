/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;
import java.sql.SQLException;
/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public final class SqlErrorClassifier {
    private SqlErrorClassifier() {}

    public static AppException map(SQLException e) {
        String state = e.getSQLState();
        int code = e.getErrorCode();
        String msg  = e.getMessage();

        // 23000 = violaciones de integridad en MySQL/MariaDB (UNIQUE/PK/FK/CHECK)
        if ("23000".equals(state)) {
            // códigos típicos
            if (code == 1062)
                {
                    String m = msg != null ? msg : "";
                    if (m.contains("uq_prop_padron") || m.contains("pro_padron_catastral"))
                        return new IntegrityException("Padrón catastral duplicado.", e);
                    if (m.contains("uq_esn_numero") || m.contains("esn_nro_escritura"))
                        return new IntegrityException("Número de escritura duplicado.", e);
                    return new IntegrityException("Dato duplicado (clave única).", e);
                }
            
            if (code == 1452) return new IntegrityException("Violación de FK (referencia inexistente).", e);
            return new IntegrityException("Violación de integridad de datos.", e);
        }

        // Otros estados frecuentes
        if ("40001".equals(state)) { // deadlock / serialization
            return new DatabaseException("Deadlock detectado. Intente nuevamente.", e);
        }

        // genérico
        return new DatabaseException("Error de base de datos: " + msg, e);
    }
}
