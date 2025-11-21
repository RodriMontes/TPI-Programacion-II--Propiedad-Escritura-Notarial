/* =========================================================
   TFI Programación 2 - Script Único
   Dominio: Propiedad -> Escritura Notarial (1:1)
   Alumno: Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
   ========================================================= */

/* =========================================================
   SECCIÓN 1 - CREACIÓN DE BASE DE DATOS Y TABLAS PRINCIPALES
   ========================================================= */

DROP DATABASE IF EXISTS propiedades_db;
CREATE DATABASE propiedades_db;
USE propiedades_db;

-- Tabla PROPIEDAD
DROP TABLE IF EXISTS propiedad;
CREATE TABLE propiedad (
    pro_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pro_eliminado BOOLEAN DEFAULT FALSE,
    pro_padron_catastral VARCHAR(30) UNIQUE NOT NULL,
    pro_direccion VARCHAR(150) NOT NULL,
    pro_superficie_m2 DECIMAL(10,2) NOT NULL CHECK (pro_superficie_m2 > 0),
    pro_destino ENUM('RES', 'COM') NOT NULL,
    pro_antiguedad INT CHECK (pro_antiguedad >= 0),
    pro_email VARCHAR(120),

    CHECK (pro_superficie_m2 > 0),
    CHECK (pro_antiguedad >= 0),
    CHECK (pro_padron_catastral REGEXP '^PC-[0-9]{6}-(19[5-9][0-9]|200[0-9]|201[0-9]|202[0-5])$'),
    CHECK (pro_email IS NULL OR pro_email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Tabla ESCRITURA_NOTARIAL
DROP TABLE IF EXISTS escritura_notarial;
CREATE TABLE escritura_notarial (
    esn_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    esn_eliminado BOOLEAN DEFAULT FALSE,
    esn_nro_escritura VARCHAR(30) UNIQUE NOT NULL,
    esn_fecha DATE NOT NULL,
    esn_notaria VARCHAR(120),
    esn_tomo VARCHAR(10),
    esn_folio VARCHAR(10),
    esn_observaciones VARCHAR(255),
    esn_propiedad_id BIGINT UNIQUE NULL,

    FOREIGN KEY (esn_propiedad_id)
        REFERENCES propiedad(pro_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- Tabla de Auditoría (para seguridad/concurrencia)
DROP TABLE IF EXISTS auditoria_accesos;
CREATE TABLE auditoria_accesos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    operacion ENUM('SELECT', 'INSERT', 'UPDATE', 'DELETE') NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_cliente VARCHAR(45),
    detalles TEXT
);

-- Triggers de validación de fecha de escritura
DELIMITER $$
CREATE TRIGGER tr_escritura_fecha_valida_insert
BEFORE INSERT ON escritura_notarial
FOR EACH ROW
BEGIN
    IF NEW.esn_fecha > CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La fecha de escritura no puede ser futura';
    END IF;
END$$

CREATE TRIGGER tr_escritura_fecha_valida_update
BEFORE UPDATE ON escritura_notarial
FOR EACH ROW
BEGIN
    IF NEW.esn_fecha > CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La fecha de escritura no puede ser futura';
    END IF;
END$$
DELIMITER ;


/* =========================================================
   SECCIÓN 2 - TABLAS SEMILLA Y DATOS INICIALES
   ========================================================= */

USE propiedades_db;

DROP TABLE IF EXISTS semilla_direcciones;
CREATE TABLE semilla_direcciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    calle VARCHAR(80) NOT NULL,
    numero_base INT NOT NULL
);

DROP TABLE IF EXISTS semilla_notarias;
CREATE TABLE semilla_notarias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL
);

DROP TABLE IF EXISTS semilla_observaciones;
CREATE TABLE semilla_observaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    texto VARCHAR(255) NOT NULL
);

TRUNCATE TABLE semilla_direcciones;
INSERT INTO semilla_direcciones (calle, numero_base) VALUES
('Av. Corrientes', 100),
('Av. Santa Fe', 200),
('Av. Rivadavia', 300),
('Calle Florida', 50),
('Av. Cabildo', 400),
('Av. Directorio', 500),
('Av. Jujuy', 600),
('Av. La Plata', 700),
('Av. Nazca', 800),
('Av. Acoyte', 900),
('Av. Boedo', 1000),
('Av. Independencia', 1100);

TRUNCATE TABLE semilla_notarias;
INSERT INTO semilla_notarias (nombre) VALUES
('Notaría Central'),
('Notaría Norte'),
('Notaría Sur'),
('Notaría Este'),
('Notaría Oeste'),
('Notaría Centro');

TRUNCATE TABLE semilla_observaciones;
INSERT INTO semilla_observaciones (texto) VALUES
('Escritura de compraventa'),
('Transacción inmobiliaria'),
('Herencia familiar'),
('Donación'),
('Permuta'),
('Subdivisión de terreno'),
('Unificación de parcelas');

-- Datos de prueba simples
SET SQL_SAFE_UPDATES = 0;
DELETE FROM escritura_notarial;
DELETE FROM propiedad;
SET SQL_SAFE_UPDATES = 1;

INSERT INTO propiedad (
    pro_padron_catastral, pro_direccion, pro_superficie_m2, pro_destino, pro_antiguedad
) VALUES
('PC-978581-2024', 'Av. Siempre Viva 742', 150.50, 'RES', 10),
('PC-665452-2024', 'Calle Falsa 123',       200.75, 'COM', 5),
('PC-541653-2024', 'Calle Concurrencia 1',  100.00, 'RES', 5),
('PC-541354-2024', 'Calle Concurrencia 2',  150.00, 'COM', 3),
('PC-564875-2024', 'Calle Concurrencia 3',  200.00, 'RES', 8);


/* =========================================================
   SECCIÓN 3 - CARGA MASIVA CONTROLADA (CTE + FUNCIÓN)
   ========================================================= */

USE propiedades_db;

DROP FUNCTION IF EXISTS random_deterministic;

SET autocommit = 0;
SET unique_checks = 0;
SET foreign_key_checks = 0;

DELIMITER //
CREATE FUNCTION random_deterministic(seq_num BIGINT, salt VARCHAR(50))
RETURNS DOUBLE DETERMINISTIC
BEGIN
    RETURN (CONV(SUBSTR(SHA2(CONCAT(seq_num, '|', salt), 256), 1, 16), 16, 10) % 1000000) / 1000000;
END//
DELIMITER ;

DROP TEMPORARY TABLE IF EXISTS tmp_secuencia;
DROP TEMPORARY TABLE IF EXISTS tmp_digitos;

-- Tabla temporal para secuencia de números
CREATE TEMPORARY TABLE tmp_secuencia (
    seq_num INT PRIMARY KEY
) ENGINE = InnoDB;

-- Tabla temporal de dígitos 0-9
CREATE TEMPORARY TABLE tmp_digitos (
    d TINYINT PRIMARY KEY
) ENGINE = InnoDB;

INSERT INTO tmp_digitos (d) VALUES
(0),(1),(2),(3),(4),(5),(6),(7),(8),(9);

-- Generamos números del 1 al 100000
INSERT INTO tmp_secuencia (seq_num)
SELECT 
    d1.d 
  + d2.d * 10 
  + d3.d * 100 
  + d4.d * 1000 
  + d5.d * 10000 
  + 1 AS seq_num
FROM tmp_digitos d1
CROSS JOIN tmp_digitos d2
CROSS JOIN tmp_digitos d3
CROSS JOIN tmp_digitos d4
CROSS JOIN tmp_digitos d5
WHERE 
    d1.d 
  + d2.d * 10 
  + d3.d * 100 
  + d4.d * 1000 
  + d5.d * 10000 
  + 1 <= 25000;

COMMIT;

-- Ya no necesitamos la tabla de dígitos
DROP TEMPORARY TABLE IF EXISTS tmp_digitos;


-- Carga masiva de propiedades
INSERT IGNORE INTO propiedad (
    pro_padron_catastral,
    pro_direccion,
    pro_superficie_m2,
    pro_destino,
    pro_antiguedad
)
SELECT 
    CONCAT(
        'PC-',
        LPAD(s.seq_num, 6, '0'),
        '-',
        2025 - FLOOR(random_deterministic(s.seq_num, 'antiguedad') * 50)
    ) AS pro_padron_catastral,
    CONCAT(d.calle, ' ', (d.numero_base + MOD(s.seq_num, 100))),
    ROUND(40 + (random_deterministic(s.seq_num, 'superficie') * 460), 2),
    CASE 
        WHEN random_deterministic(s.seq_num, 'destino') < 0.6 THEN 'RES'
        ELSE 'COM'
    END,
    FLOOR(random_deterministic(s.seq_num, 'antiguedad') * 50) AS pro_antiguedad
FROM tmp_secuencia s
CROSS JOIN semilla_direcciones d
WHERE MOD(s.seq_num, 12) + 1 = d.id;

COMMIT;

-- Carga masiva de escrituras (90% de propiedades)
INSERT IGNORE INTO escritura_notarial (
    esn_nro_escritura,
    esn_fecha,
    esn_notaria,
    esn_tomo,
    esn_folio,
    esn_observaciones,
    esn_propiedad_id
)
SELECT
    CONCAT(
        'ESC-',
        LPAD(p.pro_id, 6, '0'),
        '-',
        YEAR(
            DATE_SUB(
                CURDATE(),
                INTERVAL FLOOR(random_deterministic(p.pro_id, 'fecha') * 1825) DAY
            )
        )
    ) AS esn_nro_escritura,
    DATE_SUB(
        CURDATE(),
        INTERVAL FLOOR(random_deterministic(p.pro_id, 'fecha') * 1825) DAY
    ) AS esn_fecha,
    (SELECT nombre
     FROM semilla_notarias
     ORDER BY random_deterministic(p.pro_id, 'notaria')
     LIMIT 1),
    CONCAT('T', FLOOR(1 + random_deterministic(p.pro_id, 'tomo') * 10)),
    CONCAT('F', FLOOR(1 + random_deterministic(p.pro_id, 'folio') * 100)),
    (SELECT texto
     FROM semilla_observaciones
     ORDER BY random_deterministic(p.pro_id, 'obs')
     LIMIT 1),
    p.pro_id
FROM propiedad p
WHERE MOD(p.pro_id, 10) <> 0;

COMMIT;

SET autocommit = 1;
SET unique_checks = 1;
SET foreign_key_checks = 1;

DROP TABLE IF EXISTS semilla_direcciones;
DROP TABLE IF EXISTS semilla_notarias;
DROP TABLE IF EXISTS semilla_observaciones;


/* =========================================================
   SECCIÓN 4 - ÍNDICES PARA PERFORMANCE
   ========================================================= */

USE propiedades_db;

-- Limpieza (por si se re-ejecuta el script)
DROP INDEX IF EXISTS idx_pro_filtros_compuesto ON propiedad;
DROP INDEX IF EXISTS idx_pro_busqueda          ON propiedad;
DROP INDEX IF EXISTS idx_esn_filtros_compuesto ON escritura_notarial;
DROP INDEX IF EXISTS idx_esn_notaria_eliminado ON escritura_notarial;
DROP INDEX IF EXISTS idx_esn_anti_join         ON escritura_notarial;
DROP INDEX IF EXISTS idx_auditoria_usuario     ON auditoria_accesos;
DROP INDEX IF EXISTS idx_auditoria_fecha       ON auditoria_accesos;
DROP INDEX IF EXISTS idx_auditoria_tabla       ON auditoria_accesos;

-- Índices en PROPIEDAD
CREATE INDEX idx_pro_filtros_compuesto
    ON propiedad (pro_eliminado, pro_destino, pro_superficie_m2);
CREATE INDEX idx_pro_busqueda
    ON propiedad (pro_eliminado, pro_destino, pro_antiguedad);

-- Índices en ESCRITURA_NOTARIAL
CREATE INDEX idx_esn_filtros_compuesto
    ON escritura_notarial (esn_eliminado, esn_fecha, esn_propiedad_id);
CREATE INDEX idx_esn_notaria_eliminado
    ON escritura_notarial (esn_eliminado, esn_notaria);
CREATE INDEX idx_esn_anti_join
    ON escritura_notarial (esn_propiedad_id, esn_eliminado);

-- Índices en auditoría
CREATE INDEX idx_auditoria_usuario ON auditoria_accesos(usuario);
CREATE INDEX idx_auditoria_fecha   ON auditoria_accesos(fecha_hora);
CREATE INDEX idx_auditoria_tabla   ON auditoria_accesos(tabla_afectada);


/* =========================================================
   SECCIÓN 5 - VISTAS DE NEGOCIO
   ========================================================= */

USE propiedades_db;

DROP VIEW IF EXISTS v_propiedades_completas;
CREATE VIEW v_propiedades_completas AS
SELECT
    p.pro_id,
    p.pro_padron_catastral,
    p.pro_direccion,
    p.pro_superficie_m2,
    p.pro_destino,
    p.pro_antiguedad,
    p.pro_eliminado,
    e.esn_id,
    e.esn_nro_escritura,
    e.esn_fecha,
    e.esn_notaria,
    e.esn_tomo,
    e.esn_folio,
    e.esn_observaciones,
    e.esn_eliminado,
    CASE WHEN e.esn_id IS NOT NULL THEN 1 ELSE 0 END AS tiene_escritura,
    CASE
        WHEN p.pro_antiguedad < 5 THEN 'NUEVA'
        WHEN p.pro_antiguedad BETWEEN 5 AND 20 THEN 'MEDIA'
        ELSE 'ANTIGUA'
    END AS categoria_antiguedad,
    CASE
        WHEN p.pro_superficie_m2 < 80 THEN 'PEQUEÑA'
        WHEN p.pro_superficie_m2 BETWEEN 80 AND 150 THEN 'MEDIANA'
        ELSE 'GRANDE'
    END AS categoria_superficie
FROM propiedad p
LEFT JOIN escritura_notarial e ON p.pro_id = e.esn_propiedad_id
WHERE p.pro_eliminado = FALSE;

DROP VIEW IF EXISTS v_busqueda_avanzada;
CREATE VIEW v_busqueda_avanzada AS
SELECT
    p.pro_id,
    p.pro_padron_catastral,
    p.pro_direccion,
    p.pro_superficie_m2,
    p.pro_destino,
    p.pro_antiguedad,
    e.esn_nro_escritura,
    e.esn_fecha,
    e.esn_notaria,
    e.esn_tomo,
    e.esn_folio,
    LOWER(CONCAT(
        p.pro_direccion, ' ',
        p.pro_padron_catastral, ' ',
        COALESCE(e.esn_notaria, ''), ' ',
        COALESCE(e.esn_nro_escritura, '')
    )) AS texto_busqueda,
    YEAR(e.esn_fecha)  AS anio_escritura,
    MONTH(e.esn_fecha) AS mes_escritura,
    CASE
        WHEN p.pro_superficie_m2 < 50 THEN '0-50'
        WHEN p.pro_superficie_m2 BETWEEN 50 AND 100 THEN '50-100'
        WHEN p.pro_superficie_m2 BETWEEN 100 AND 200 THEN '100-200'
        ELSE '200+'
    END AS rango_superficie,
    CASE
        WHEN p.pro_antiguedad < 5 THEN '0-5'
        WHEN p.pro_antiguedad BETWEEN 5 AND 15 THEN '5-15'
        WHEN p.pro_antiguedad BETWEEN 15 AND 30 THEN '15-30'
        ELSE '30+'
    END AS rango_antiguedad
FROM propiedad p
LEFT JOIN escritura_notarial e ON p.pro_id = e.esn_propiedad_id
WHERE p.pro_eliminado = FALSE;

DROP VIEW IF EXISTS v_propiedades_residenciales;
CREATE VIEW v_propiedades_residenciales AS
SELECT *
FROM propiedad
WHERE pro_destino = 'RES'
  AND pro_eliminado = FALSE;

DROP VIEW IF EXISTS v_escrituras_recientes;
CREATE VIEW v_escrituras_recientes AS
SELECT *
FROM escritura_notarial
WHERE esn_fecha >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR);


/* =========================================================
   SECCIÓN 6 - VISTAS DE SEGURIDAD / ANONIMIZACIÓN
   ========================================================= */

USE propiedades_db;

DROP VIEW IF EXISTS v_estadisticas_anonimas;
CREATE VIEW v_estadisticas_anonimas AS
SELECT
    COUNT(*) AS total_propiedades,
    ROUND((SUM(CASE WHEN pro_destino = 'RES' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_residencial,
    ROUND((SUM(CASE WHEN pro_destino = 'COM' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_comercial,
    ROUND((SUM(CASE WHEN pro_superficie_m2 < 50 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_pequenas,
    ROUND((SUM(CASE WHEN pro_superficie_m2 BETWEEN 50 AND 150 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_medianas,
    ROUND((SUM(CASE WHEN pro_superficie_m2 > 150 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_grandes,
    ROUND(AVG(pro_superficie_m2), 1) AS superficie_promedio_m2,
    ROUND(AVG(pro_antiguedad), 1)    AS antiguedad_promedio_anios,
    (SELECT COUNT(*)
     FROM escritura_notarial
     WHERE esn_fecha >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
       AND esn_eliminado = FALSE) AS escrituras_ultimo_mes
FROM propiedad
WHERE pro_eliminado = FALSE;

DROP VIEW IF EXISTS v_propiedades_publicas;
CREATE VIEW v_propiedades_publicas AS
SELECT
    CONCAT('Zona ', SUBSTRING(pro_padron_catastral, 1, 3)) AS zona,
    CASE
        WHEN pro_superficie_m2 < 50 THEN 'PEQUEÑA'
        WHEN pro_superficie_m2 BETWEEN 50 AND 150 THEN 'MEDIANA'
        ELSE 'GRANDE'
    END AS categoria_superficie,
    CASE
        WHEN pro_antiguedad < 5 THEN 'NUEVA'
        WHEN pro_antiguedad BETWEEN 5 AND 20 THEN 'MEDIA'
        ELSE 'ANTIGUA'
    END AS categoria_antiguedad,
    pro_destino AS tipo_propiedad,
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM escritura_notarial e
            WHERE e.esn_propiedad_id = p.pro_id
              AND e.esn_eliminado = FALSE
        ) THEN 'REGISTRADA'
        ELSE 'NO_REGISTRADA'
    END AS estado_registro,
    (SELECT COUNT(*)
     FROM propiedad p2
     WHERE p2.pro_eliminado = FALSE
       AND SUBSTRING(p2.pro_padron_catastral, 1, 3) = SUBSTRING(p.pro_padron_catastral, 1, 3)
    ) AS propiedades_en_zona
FROM propiedad p
WHERE p.pro_eliminado = FALSE;

DROP VIEW IF EXISTS v_mapa_anonimo;
CREATE VIEW v_mapa_anonimo AS
SELECT
    CONCAT('Sector-', FLOOR(RAND() * 100)) AS sector,
    COUNT(*) AS densidad,
    ROUND((SUM(CASE WHEN pro_destino = 'RES' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_residencial,
    ROUND((SUM(CASE WHEN pro_destino = 'COM' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 1) AS porcentaje_comercial,
    ROUND(AVG(pro_superficie_m2), 1) AS superficie_promedio_sector,
    ROUND(AVG(pro_antiguedad), 1)    AS antiguedad_promedio_sector
FROM propiedad
WHERE pro_eliminado = FALSE
GROUP BY CONCAT('Sector-', FLOOR(RAND() * 100));


/* =========================================================
   SECCIÓN 7 - USUARIOS, ROLES Y TRIGGERS DE AUDITORÍA
   ========================================================= */

USE propiedades_db;

DROP USER IF EXISTS 'admin_propiedades'@'localhost';
DROP USER IF EXISTS 'admin_propiedades'@'%';
DROP USER IF EXISTS 'web_propiedades'@'localhost';
DROP USER IF EXISTS 'web_propiedades'@'%';

CREATE USER 'admin_propiedades'@'localhost' IDENTIFIED BY 'Admin2024Secure!';
CREATE USER 'admin_propiedades'@'%'        IDENTIFIED BY 'Admin2024Secure!';

GRANT ALL PRIVILEGES ON propiedades_db.* TO 'admin_propiedades'@'localhost';
GRANT ALL PRIVILEGES ON propiedades_db.* TO 'admin_propiedades'@'%';

CREATE USER 'web_propiedades'@'localhost' IDENTIFIED BY 'Web2024Secure!';
CREATE USER 'web_propiedades'@'%'        IDENTIFIED BY 'Web2024Secure!';

GRANT SELECT, INSERT, UPDATE
    ON propiedades_db.propiedad
    TO 'web_propiedades'@'localhost',
       'web_propiedades'@'%';

GRANT SELECT, INSERT, UPDATE
    ON propiedades_db.escritura_notarial
    TO 'web_propiedades'@'localhost',
       'web_propiedades'@'%';

GRANT SELECT
    ON propiedades_db.v_estadisticas_anonimas
    TO 'web_propiedades'@'localhost',
       'web_propiedades'@'%';

GRANT SELECT
    ON propiedades_db.v_propiedades_publicas
    TO 'web_propiedades'@'localhost',
       'web_propiedades'@'%';

GRANT SELECT
    ON propiedades_db.v_mapa_anonimo
    TO 'web_propiedades'@'localhost',
       'web_propiedades'@'%';

FLUSH PRIVILEGES;

-- Triggers de auditoría
DROP TRIGGER IF EXISTS trig_auditoria_propiedad_insert;
DROP TRIGGER IF EXISTS trig_auditoria_propiedad_update;
DROP TRIGGER IF EXISTS trig_auditoria_escritura_insert;

DELIMITER //

CREATE TRIGGER trig_auditoria_propiedad_insert
AFTER INSERT ON propiedad
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, ip_cliente, detalles)
    VALUES (USER(), 'propiedad', 'INSERT', SUBSTRING_INDEX(USER(), '@', -1),
            CONCAT('Nueva propiedad: ', NEW.pro_padron_catastral));
END//

CREATE TRIGGER trig_auditoria_propiedad_update
AFTER UPDATE ON propiedad
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, ip_cliente, detalles)
    VALUES (USER(), 'propiedad', 'UPDATE', SUBSTRING_INDEX(USER(), '@', -1),
            CONCAT('Actualizada propiedad ID: ', OLD.pro_id));
END//

CREATE TRIGGER trig_auditoria_escritura_insert
AFTER INSERT ON escritura_notarial
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, ip_cliente, detalles)
    VALUES (USER(), 'escritura_notarial', 'INSERT', SUBSTRING_INDEX(USER(), '@', -1),
            CONCAT('Nueva escritura: ', NEW.esn_nro_escritura,
                   ' para propiedad: ', NEW.esn_propiedad_id));
END//

DELIMITER ;


/* =========================================================
   SECCIÓN 8 - PROCEDIMIENTOS DE TRANSACCIONES
   ========================================================= */

USE propiedades_db;

DROP PROCEDURE IF EXISTS sp_crear_propiedad_con_escritura;
DROP PROCEDURE IF EXISTS sp_actualizar_propiedad_segura;

DELIMITER //

CREATE PROCEDURE sp_crear_propiedad_con_escritura(
    IN p_padron_catastral VARCHAR(30),
    IN p_direccion VARCHAR(150),
    IN p_superficie_m2 DECIMAL(10,2),
    IN p_destino ENUM('RES', 'COM'),
    IN p_nro_escritura VARCHAR(30),
    IN p_notaria VARCHAR(120)
)
BEGIN
    DECLARE v_propiedad_id BIGINT;
    DECLARE v_error_msg TEXT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_error_msg = MESSAGE_TEXT;
        ROLLBACK;

        INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
        VALUES (USER(), 'transaccion', 'ERROR',
                CONCAT('Fallo transacción. Error: ', v_error_msg,
                       '. Padrón: ', p_padron_catastral));

        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_superficie_m2 <= 0 THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'La superficie debe ser mayor a 0';
    END IF;

    INSERT INTO propiedad (
        pro_padron_catastral,
        pro_direccion,
        pro_superficie_m2,
        pro_destino,
        pro_antiguedad
    ) VALUES (
        p_padron_catastral,
        p_direccion,
        p_superficie_m2,
        p_destino,
        0
    );

    SET v_propiedad_id = LAST_INSERT_ID();

    INSERT INTO escritura_notarial (
        esn_nro_escritura,
        esn_fecha,
        esn_notaria,
        esn_propiedad_id
    ) VALUES (
        p_nro_escritura,
        CURDATE(),
        p_notaria,
        v_propiedad_id
    );

    IF (SELECT COUNT(*)
        FROM escritura_notarial
        WHERE esn_nro_escritura = p_nro_escritura
          AND esn_eliminado = FALSE) > 1 THEN
        SIGNAL SQLSTATE '45002' SET MESSAGE_TEXT = 'Número de escritura duplicado';
    END IF;

    COMMIT;

    INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
    VALUES (USER(), 'transaccion', 'SUCCESS',
            CONCAT('Propiedad y escritura creadas. ID: ', v_propiedad_id,
                   '. Padrón: ', p_padron_catastral));

END//

CREATE PROCEDURE sp_actualizar_propiedad_segura(
    IN p_pro_id BIGINT,
    IN p_nueva_direccion VARCHAR(150),
    IN p_nueva_superficie DECIMAL(10,2)
)
BEGIN
    DECLARE v_error_msg TEXT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_error_msg = MESSAGE_TEXT;
        ROLLBACK;

        INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
        VALUES (USER(), 'propiedad', 'ERROR',
                CONCAT('Fallo actualización. Error: ', v_error_msg,
                       '. Propiedad ID: ', p_pro_id));
        RESIGNAL;
    END;

    START TRANSACTION;

    IF NOT EXISTS (
        SELECT 1
        FROM propiedad
        WHERE pro_id = p_pro_id
          AND pro_eliminado = FALSE
    ) THEN
        SIGNAL SQLSTATE '45003' SET MESSAGE_TEXT = 'Propiedad no encontrada o eliminada';
    END IF;

    IF p_nueva_superficie <= 0 THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'La superficie debe ser mayor a 0';
    END IF;

    UPDATE propiedad
    SET pro_direccion    = p_nueva_direccion,
        pro_superficie_m2 = p_nueva_superficie
    WHERE pro_id = p_pro_id;

    COMMIT;

    INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
    VALUES (USER(), 'propiedad', 'UPDATE',
            CONCAT('Actualización exitosa. Propiedad ID: ', p_pro_id));
END//

DELIMITER ;


/* =========================================================
   SECCIÓN 9 - CONCURRENCIA Y DEADLOCK (PROCEDIMIENTOS + GUÍA)
   ========================================================= */

USE propiedades_db;

SET @prop1_id = (SELECT pro_id FROM propiedad WHERE pro_eliminado = FALSE LIMIT 1);
SET @prop2_id = (SELECT pro_id FROM propiedad WHERE pro_eliminado = FALSE LIMIT 1 OFFSET 1);

INSERT IGNORE INTO propiedad (
    pro_padron_catastral,
    pro_direccion,
    pro_superficie_m2,
    pro_destino,
    pro_antiguedad
) VALUES
('PC-111111-2024', 'Calle Concurrencia 111', 100.00, 'RES', 2),
('PC-222222-2024', 'Calle Concurrencia 222', 150.00, 'COM', 3);

SET @prop1_id = COALESCE(@prop1_id, (SELECT pro_id FROM propiedad WHERE pro_padron_catastral = 'PC-111111-2024'));
SET @prop2_id = COALESCE(@prop2_id, (SELECT pro_id FROM propiedad WHERE pro_padron_catastral = 'PC-222222-2024'));

DROP PROCEDURE IF EXISTS sp_actualizar_propiedad_con_retry;
DROP PROCEDURE IF EXISTS sp_test_niveles_aislamiento;

DELIMITER //

CREATE PROCEDURE sp_actualizar_propiedad_con_retry(
    IN p_pro_id BIGINT,
    IN p_nueva_superficie DECIMAL(10,2),
    IN p_max_reintentos INT
)
BEGIN
    DECLARE v_reintento INT DEFAULT 0;
    DECLARE v_exito    BOOLEAN DEFAULT FALSE;
    DECLARE v_error_code INT DEFAULT 0;
    DECLARE v_error_msg  VARCHAR(1000);

    WHILE v_reintento <= p_max_reintentos AND NOT v_exito DO
        BEGIN
            DECLARE EXIT HANDLER FOR 1213, 1205
            BEGIN
                GET DIAGNOSTICS CONDITION 1
                    v_error_code = MYSQL_ERRNO,
                    v_error_msg  = MESSAGE_TEXT;

                SET v_reintento = v_reintento + 1;

                INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
                VALUES (USER(), 'propiedad', 'DEADLOCK',
                        CONCAT('Deadlock detectado. Reintento ', v_reintento,
                               '. Error: ', v_error_msg, ' (', v_error_code, ')'));

                SELECT SLEEP(LEAST(POWER(2, v_reintento - 1), 2) * (0.8 + RAND() * 0.4));
            END;

            START TRANSACTION;

            UPDATE propiedad
            SET pro_superficie_m2 = p_nueva_superficie,
                pro_antiguedad   = pro_antiguedad + 1
            WHERE pro_id = p_pro_id;

            SELECT SLEEP(0.5);

            COMMIT;

            SET v_exito = TRUE;

            INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
            VALUES (USER(), 'propiedad', 'UPDATE',
                    CONCAT('Actualización exitosa. Propiedad ID: ', p_pro_id,
                           '. Reintentos: ', v_reintento));
        END;
    END WHILE;

    IF NOT v_exito THEN
        INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
        VALUES (USER(), 'propiedad', 'ERROR',
                CONCAT('FALLA después de ', p_max_reintentos,
                       ' reintentos. Propiedad ID: ', p_pro_id));

        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se pudo completar la operación después de los reintentos máximos';
    END IF;
END//

CREATE PROCEDURE sp_test_niveles_aislamiento(IN p_nivel_aislamiento VARCHAR(50))
BEGIN
    DECLARE v_total_propiedades INT;
    DECLARE v_total_escrituras INT;

    SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
    IF p_nivel_aislamiento = 'REPEATABLE_READ' THEN
        SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    END IF;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_total_propiedades
    FROM propiedad
    WHERE pro_eliminado = FALSE;

    SELECT CONCAT('Lectura 1 - Total propiedades: ', v_total_propiedades) AS info;

    SELECT 'Esperando 3 segundos para modificación concurrente...' AS info;
    SELECT SLEEP(3);

    SELECT COUNT(*) INTO v_total_propiedades
    FROM propiedad
    WHERE pro_eliminado = FALSE;

    SELECT CONCAT('Lectura 2 - Total propiedades: ', v_total_propiedades) AS info;

    SELECT COUNT(*) INTO v_total_escrituras
    FROM escritura_notarial
    WHERE esn_eliminado = FALSE;

    SELECT CONCAT('Total escrituras: ', v_total_escrituras) AS info;

    COMMIT;

    INSERT INTO auditoria_accesos (usuario, tabla_afectada, operacion, detalles)
    VALUES (USER(), 'sistema', 'SELECT',
            CONCAT('Prueba nivel aislamiento: ', p_nivel_aislamiento,
                   '. Propiedades: ', v_total_propiedades,
                   '. Escrituras: ', v_total_escrituras));
END//

DELIMITER ;

-- Guía para simular deadlock (EJECUTAR MANUAL EN 2 VENTANAS)
-- VENTANA 1:
--   USE propiedades_db;
--   SET autocommit = 0;
--   START TRANSACTION;
--   UPDATE propiedad SET pro_superficie_m2 = 110.00 WHERE pro_id = @prop1_id;
--   SELECT SLEEP(5);
--   UPDATE propiedad SET pro_superficie_m2 = 160.00 WHERE pro_id = @prop2_id;
--   COMMIT;
--
-- VENTANA 2:
--   USE propiedades_db;
--   SET autocommit = 0;
--   START TRANSACTION;
--   UPDATE propiedad SET pro_superficie_m2 = 155.00 WHERE pro_id = @prop2_id;
--   SELECT SLEEP(2);
--   UPDATE propiedad SET pro_superficie_m2 = 105.00 WHERE pro_id = @prop1_id;
--   COMMIT;


/* =========================================================
   SECCIÓN 10 - CONSULTAS DE VERIFICACIÓN (EJECUTAR AL FINAL)
   ========================================================= */

USE propiedades_db;

-- Verificar tablas principales
SHOW TABLES;

DESCRIBE propiedad;
DESCRIBE escritura_notarial;
DESCRIBE auditoria_accesos;

-- Contar registros clave
SELECT 'Total propiedades'      AS metric, COUNT(*) AS valor FROM propiedad
UNION ALL
SELECT 'Total escrituras', COUNT(*) FROM escritura_notarial
UNION ALL
SELECT 'Propiedades sin escritura',
       COUNT(*)
FROM propiedad p
LEFT JOIN escritura_notarial e ON p.pro_id = e.esn_propiedad_id
WHERE e.esn_id IS NULL;

-- Verificar vistas
SELECT 'Vistas creadas' AS info;
SHOW FULL TABLES WHERE TABLE_TYPE = 'VIEW';

-- Verificar que existan los procedimientos
SHOW PROCEDURE STATUS WHERE Db = 'propiedades_db';

-- Verificar algunos registros de auditoría (si se ejecutaron operaciones)
SELECT * FROM auditoria_accesos ORDER BY fecha_hora DESC LIMIT 10;
