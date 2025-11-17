# 📘 Trabajo Final Integrador – Programación 2  
### **Dominio:** Propiedad → Escritura Notarial (Relación 1:1)  
### **Aplicación de Consolola – JDBC / MySQL / MVC / DAO / SERVICES / TRANSACCIONES**

## 🧭 ** Descripción General**

Este proyecto implementa una **aplicación de consola** para la gestión de **Propiedades** y su **Escritura Notarial**, modeladas mediante una **relación 1:1 unidireccional** (una propiedad puede tener como máximo una escritura asociada).

El trabajo incluye:

- CRUD completo de Propiedad y Escritura Notarial  
- **Alta transaccional** Propiedad + Escritura  
- Validaciones en UI, Servicios y Base de Datos  
- Manejo sólido de errores mediante excepciones propias  
- Vistas SQL para reportes y consultas avanzadas  
- Triggers, índices, procedimientos almacenados  
- Auditoría y usuarios con permisos diferenciados  
- Arquitectura por capas (Config, DAO, Services, UI, DTOs, Exceptions)

---

## 🎯 ** Objetivos del Trabajo**

✔ Diseño de un sistema CRUD con arquitectura por capas  
✔ Relación **1:1** garantizada en BD  
✔ Aplicación de **DAO + JDBC**  
✔ Manejo de transacciones (**commit/rollback**)  
✔ Validaciones consistentes end-to-end  
✔ Manejo de errores con jerarquía de excepciones propia  
✔ Uso de vistas para reportes  
✔ Menú CLI robusto  
✔ Entrega de README, UML, informe, SQL único, video

---

## 🧩 ** Dominio y Relación 1:1**

### **Propiedad**
- Dirección  
- Padrón catastral  
- Superficie  
- Antigüedad  
- Destino  
- Responsable  
- Email  
- Eliminado  

### **Escritura Notarial**
- Número de escritura  
- Fecha de emisión  
- ID de Propiedad (FK UNIQUE → relación 1:1 garantizada)  
- Eliminado  

Implementación SQL:

```sql
esn_propiedad_id BIGINT UNIQUE,
FOREIGN KEY (esn_propiedad_id) REFERENCES propiedad(pro_id)
```

---

## 🏛️ ** Arquitectura del Proyecto**

```
src/
├── config
│   └── DatabaseConnection.java
├── entities
│   ├── BaseEntity.java
│   ├── Propiedad.java
│   ├── EscrituraNotarial.java
│   └── Destino.java
├── dao
│   ├── GenericDao.java
│   ├── PropiedadDao.java
│   ├── EscrituraNotarialDao.java
│   └── VistasDao.java
├── services
│   ├── GenericService.java
│   ├── PropiedadService.java
│   ├── EscrituraNotarialService.java
│   └── VistasService.java
├── exceptions
│   ├── AppException.java
│   ├── ValidationException.java
│   ├── IntegrityException.java
│   ├── NotFoundException.java
│   ├── DatabaseException.java
│   └── SqlErrorClassifier.java
├── dto
│   ├── PropiedadCompletaDto.java
│   └── DistribucionRangosDto.java
├── main
│   ├── AppMenu.java
│   └── Main.java
└── scripts
    └── SQL UNICO TP PROGRAMACION.sql
```

---

## ⚙️ ** Requisitos**

| Requisito | Versión |
|----------|---------|
| Java | 17+ |
| MySQL | 8+ |
| JDBC Driver | mysql-connector-j |
| IDE | IntelliJ / Eclipse |

---

## 🗄️ ** Instalación**

### 1. Clonar el repositorio
```bash
git clone 
cd tp-programacion2
```

### 2. Crear la base de datos
```bash
mysql -u root -p < "scripts/SQL UNICO TP PROGRAMACION.sql"
```

### 3. Configurar conexión
Editar `DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/propiedades_db";
private static final String USER = "root";
private static final String PASSWORD = "tu_clave";
```

### 4. Ejecutar
Ejecutar `Main.java`.

---

## 💼 ** Funcionalidades**

### ✔ CRUD completo  
### ✔ Baja lógica  
### ✔ Alta Transaccional  
### ✔ Vistas SQL para reportes  
### ✔ Búsquedas avanzadas  
### ✔ Excepciones personalizadas  
### ✔ Rollback ante fallos  
### ✔ Auditoría y usuarios SQL  

---

## 🗺 ** SQL y Vistas**

Incluye:

- Triggers  
- CHECK constraints  
- Vistas como:  
  - `v_propiedades_completas`  
  - `v_busqueda_avanzada`  
  - `v_escrituras_recientes`  
- Store Procedures  
  - `sp_crear_propiedad_con_escritura`  
  - `sp_actualizar_propiedad_segura`  
  - SPs para deadlocks  
- Auditoría y roles SQL


---

## 👥 ** Integrantes**

- Rodrigo Montes Sare – 
- Laura Mendez – 
- Maximiliano Montaña – 

---

## 🚀 ** Mejoras Futuras**

- Exportar a PDF/CSV  
- API REST con Spring  
- JavaFX UI  
- Logs persistentes  
- Testing con JUnit  

---

## ✅ ** Conclusión**

El sistema cumple todos los requisitos del TFI:  
✔ Relación 1:1  
✔ Transacciones  
✔ Vistas SQL  
✔ CRUD completo  
✔ Manejo de excepciones  
✔ Arquitectura modular  
✔ Script SQL único  
✔ Video + README  
✔ UML + Informe  
