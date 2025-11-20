/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;
import entities.Destino;
import entities.EscrituraNotarial;
import entities.Propiedad;
import services.EscrituraNotarialService;
import services.PropiedadService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import services.VistasService;

import exceptions.AppException;
import exceptions.DatabaseException;
import exceptions.IntegrityException;
import exceptions.NotFoundException;
import exceptions.ValidationException;



/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class AppMenu {

    private final Scanner scanner;
    private final PropiedadService propiedadService;
    private final EscrituraNotarialService escrituraService;
    private final VistasService vistasService;
    private static final String PADRON_REGEX = "^PC-\\d{6}-(19[5-9]\\d|200\\d|201\\d|202[0-5])$";

    public AppMenu() {
        this.scanner = new Scanner(System.in);
        this.propiedadService = new PropiedadService();
        this.escrituraService = new EscrituraNotarialService();
        this.vistasService = new VistasService();
    }

    public void iniciar() {
        int opcion;
        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opción: ");

            try {
                switch (opcion) {
                    case 1 -> altaPropiedad();
                    case 2 -> listarPropiedades();
                    case 3 -> actualizarPropiedad();
                    case 4 -> eliminarPropiedad();
                    case 5 -> buscarPropiedadPorPadron();
                    case 6 -> altaEscritura();
                    case 7 -> listarEscrituras();
                    case 8 -> actualizarEscritura();
                    case 9 -> eliminarEscritura();
                    case 10 -> buscarEscrituraPorNumero();
                    case 11 -> altaPropiedadConEscrituraTransaccional();
                    case 12 -> rptPropiedadesCompletasVista();
                    case 13 -> busquedaAvanzadaVista();
                    case 0 -> System.out.println("Saliendo del sistema...");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (ValidationException e) {
                System.out.println("Datos inválidos: " + e.getMessage());
            } catch (IntegrityException e) {
                System.out.println("Integridad: " + e.getMessage());
            } catch (NotFoundException e) {
                System.out.println("No encontrado: " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (AppException e) {
                System.out.println("Error de aplicación: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
            System.out.println();
        } while (opcion != 0);
    }

    private void mostrarMenuPrincipal() {
        System.out.println("===== SISTEMA PROPIEDADES / ESCRITURAS =====");
        System.out.println("1. Alta Propiedad");
        System.out.println("2. Listar Propiedades");
        System.out.println("3. Actualizar Propiedad");
        System.out.println("4. Eliminar Propiedad (lógico)");
        System.out.println("5. Buscar Propiedad por padrón");
        System.out.println("6. Alta Escritura Notarial");
        System.out.println("7. Listar Escrituras");
        System.out.println("8. Actualizar Escritura Notarial");
        System.out.println("9. Eliminar Escritura (lógico)");
        System.out.println("10. Buscar Escritura por número");
        System.out.println("11. Alta Propiedad + Escritura (TRANSACCIÓN)");
        System.out.println("=== VISTAS / REPORTES ===");
        System.out.println("12. Propiedades completas (vista)");
        System.out.println("13. Búsqueda avanzada (vista)");
        System.out.println("0. Salir");
    }

    // =========================
    // Propiedad
    // =========================

    private void altaPropiedad() throws AppException {
        System.out.println("=== Alta de Propiedad ===");

        String padron = leerTexto("Padrón catastral: ");
        String direccion = leerTexto("Dirección: ");
        BigDecimal superficie = new BigDecimal(leerTexto("Superficie (m2): "));

        Destino destino = leerDestinoInteractivo();

        Integer antiguedad = leerEntero("Antigüedad (años, puede ser 0): ");
        String email = leerTextoOpcional("Email (opcional): ");

        Propiedad p = new Propiedad();
        p.setPadronCatastral(padron);
        p.setDireccion(direccion);
        p.setSuperficieM2(superficie);
        p.setDestino(destino);
        p.setAntiguedad(antiguedad);
        p.setEmail(email);

        propiedadService.insertar(p);
        System.out.println("Propiedad creada con ID: " + p.getId());
    }
    
    private void altaPropiedadConEscrituraTransaccional() {
        System.out.println("=== Alta Propiedad + Escritura (TRANSACCIÓN) ===");
        try {
            // ---- Datos propiedad ----
            String padron = leerTexto("Padrón catastral: ").trim();
            if (!padron.matches(PADRON_REGEX)) {
                System.out.println("Formato de padrón inválido (PC-######-1950-2025).");
                return;
            }

            String direccion = leerTexto("Dirección: ").trim();
            if (direccion.isBlank()) { System.out.println("La dirección es obligatoria."); return; }

            java.math.BigDecimal superficie;
            try {
                superficie = new java.math.BigDecimal(leerTexto("Superficie (m2): ").trim());
                if (superficie.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    System.out.println("La superficie debe ser > 0."); return;
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Superficie inválida."); return;
            }

            Destino destino = leerDestinoInteractivo();

            Integer antiguedad = leerEntero("Antigüedad (años): ");
            if (antiguedad != null && antiguedad < 0) {
                System.out.println("La antigüedad no puede ser negativa."); return;
            }

            String email = leerTextoOpcional("Email (opcional): ").trim();
            if (email.isBlank()) email = null;
            else if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("Email inválido."); return;
            }

            var p = new entities.Propiedad();
            p.setPadronCatastral(padron);
            p.setDireccion(direccion);
            p.setSuperficieM2(superficie);
            p.setDestino(destino);
            p.setAntiguedad(antiguedad);
            p.setEmail(email);

            // ---- Datos escritura ----
            String nro = leerTexto("Número de escritura: ").trim();
            if (nro.isBlank()) { System.out.println("El número de escritura es obligatorio."); return; }

            java.time.LocalDate fecha;
            try {
                fecha = java.time.LocalDate.parse(leerTexto("Fecha (AAAA-MM-DD): ").trim());
            } catch (Exception pe) {
                System.out.println("Fecha inválida (use AAAA-MM-DD)."); return;
            }

            String notaria = leerTexto("Notaría: ").trim();
            if (notaria.isBlank()) { System.out.println("La notaría es obligatoria."); return; }
            String tomo = leerTexto("Tomo: ").trim();
            if (tomo.isBlank()) { System.out.println("El tomo es obligatorio."); return; }
            String folio = leerTexto("Folio: ").trim();
            if (folio.isBlank()) { System.out.println("El folio es obligatorio."); return; }
            String obs = leerTextoOpcional("Observaciones (opcional): ").trim();
            if (obs.isBlank()) obs = null;

            var e = new entities.EscrituraNotarial();
            e.setNroEscritura(nro);
            e.setFecha(fecha);
            e.setNotaria(notaria);
            e.setTomo(tomo);
            e.setFolio(folio);
            e.setObservaciones(obs);

            propiedadService.altaPropiedadConEscrituraTransaccional(p, e);

            System.out.println("Operación OK. Propiedad ID: " + p.getId() + " | Escritura ID: " + e.getId());

        } catch (ValidationException ve) {
            System.out.println("Datos inválidos: " + ve.getMessage());
        } catch (IntegrityException ie) {
            System.out.println("Integridad: " + ie.getMessage());
            System.out.println("La operación fue revertida: no se guardó ningún cambio.");
        } catch (DatabaseException de) {
            System.out.println("Error de base de datos: " + de.getMessage());
        } catch (AppException ae) {
            System.out.println("Error: " + ae.getMessage());
        } catch (Exception ex) {
            System.out.println("Error inesperado: " + ex.getMessage());
        }
    }
    
    private entities.Destino leerDestinoInteractivo() {
        entities.Destino destino = null;
        while (destino == null) {
            String in = leerTexto("Destino (RES / COM): ").trim().toUpperCase();
            if (in.isEmpty()) {
                System.out.println("✗ El destino es obligatorio.");
                continue;
            }
            if (in.equals("RES") || in.equals("COM")) {
                destino = entities.Destino.valueOf(in);
            } else {
                System.out.println("✗ Destino inválido. Use RES o COM.");
            }
        }
        return destino;
    }


    private void listarPropiedades() throws AppException {
        System.out.println("=== Listado de Propiedades ===");
        List<Propiedad> lista = propiedadService.getAll();
        if (lista.isEmpty()) {
            System.out.println("No hay propiedades cargadas.");
        } else {
            for (Propiedad p : lista) {
                System.out.println(p);
            }
        }
    }

    private void actualizarPropiedad() throws AppException {
        System.out.println("=== Actualizar Propiedad ===");
        Long id = leerLong("ID de la propiedad: ");

        Propiedad p = propiedadService.getById(id);
        if (p == null) {
            System.out.println("No se encontró la propiedad con ID " + id);
            return;
        }

        System.out.println("Propiedad actual:");
        System.out.println(p);

        String padron = leerTextoOpcional("Nuevo padrón (ENTER para mantener): ");
        if (!padron.isBlank()) p.setPadronCatastral(padron);

        String direccion = leerTextoOpcional("Nueva dirección (ENTER para mantener): ");
        if (!direccion.isBlank()) p.setDireccion(direccion);

        String supStr = leerTextoOpcional("Nueva superficie m2 (ENTER para mantener): ");
        if (!supStr.isBlank()) p.setSuperficieM2(new BigDecimal(supStr));

        String destStr = leerTextoOpcional("Nuevo destino RES/COM (ENTER para mantener): ");
        if (!destStr.isBlank()) p.setDestino(Destino.valueOf(destStr.toUpperCase()));

        String antigStr = leerTextoOpcional("Nueva antigüedad (ENTER para mantener): ");
        if (!antigStr.isBlank()) p.setAntiguedad(Integer.parseInt(antigStr));

        String email = leerTextoOpcional("Nuevo email (ENTER para mantener): ");
        if (!email.isBlank()) p.setEmail(email);

        propiedadService.actualizar(p);
        System.out.println("Propiedad actualizada.");
    }

    private void eliminarPropiedad() throws AppException {
        System.out.println("=== Eliminar Propiedad (lógico) ===");
        Long id = leerLong("ID de la propiedad: ");
        propiedadService.eliminar(id);
        System.out.println("Propiedad marcada como eliminada.");
    }

    private void buscarPropiedadPorPadron() throws AppException {
        System.out.println("=== Buscar Propiedad por padrón ===");
        String padron = leerTexto("Padrón catastral: ");

        Propiedad p = propiedadService.buscarPorPadron(padron);
        if (p == null) {
            System.out.println("No se encontró propiedad con ese padrón.");
        } else {
            System.out.println(p);
        }
    }

    // =========================
    // Escritura Notarial
    // =========================

    private void altaEscritura() throws AppException {
        System.out.println("=== Alta de Escritura Notarial ===");

        String nro = leerTexto("Número de escritura: ");
        String fechaStr = leerTexto("Fecha (AAAA-MM-DD): ");
        LocalDate fecha = LocalDate.parse(fechaStr);

        String notaria = leerTexto("Notaría: ");
        String tomo = leerTexto("Tomo: ");
        String folio = leerTexto("Folio: ");
        String obs = leerTextoOpcional("Observaciones (opcional): ");

        Long propiedadId = leerLong("ID de la propiedad asociada: ");

        EscrituraNotarial e = new EscrituraNotarial();
        e.setNroEscritura(nro);
        e.setFecha(fecha);
        e.setNotaria(notaria);
        e.setTomo(tomo);
        e.setFolio(folio);
        e.setObservaciones(obs);
        e.setPropiedadId(propiedadId);

        escrituraService.insertar(e);
        System.out.println("Escritura creada con ID: " + e.getId());
    }

    private void listarEscrituras() throws AppException {
        System.out.println("=== Listado de Escrituras ===");
        List<EscrituraNotarial> lista = escrituraService.getAll();
        if (lista.isEmpty()) {
            System.out.println("No hay escrituras cargadas.");
        } else {
            for (EscrituraNotarial e : lista) {
                System.out.println(e);
            }
        }
    }

    private void eliminarEscritura() throws AppException {
        System.out.println("=== Eliminar Escritura (lógico) ===");
        Long id = leerLong("ID de la escritura: ");
        escrituraService.eliminar(id);
        System.out.println("Escritura marcada como eliminada.");
    }

    private void buscarEscrituraPorNumero() throws AppException {
        System.out.println("=== Buscar Escritura por número ===");
        String nro = leerTexto("Número de escritura: ");

        EscrituraNotarial e = escrituraService.buscarPorNumero(nro);
        if (e == null) {
            System.out.println("No se encontró escritura con ese número.");
        } else {
            System.out.println(e);
        }
    }
    
    private void actualizarEscritura() throws Exception {
        System.out.println("=== Actualizar Escritura Notarial ===");
        Long id = leerLong("ID de la escritura: ");

        // Traemos la escritura actual
        EscrituraNotarial e = escrituraService.getById(id);
        if (e == null) {
            System.out.println("No se encontró la escritura con ID " + id);
            return;
        }

        // Mostramos estado actual
        System.out.println("Escritura actual:");
        System.out.println(e);

        // Campos opcionales: ENTER = mantener
        String nro = leerTextoOpcional("Nuevo número de escritura (ENTER para mantener): ").trim();
        if (!nro.isEmpty()) e.setNroEscritura(nro);

        String fechaStr = leerTextoOpcional("Nueva fecha (AAAA-MM-DD, ENTER para mantener): ").trim();
        if (!fechaStr.isEmpty()) {
            try {
                e.setFecha(java.time.LocalDate.parse(fechaStr));
            } catch (Exception pe) {
                System.out.println("✗ Fecha inválida. Se mantiene la anterior.");
            }
        }

        String notaria = leerTextoOpcional("Nueva notaría (ENTER para mantener): ").trim();
        if (!notaria.isEmpty()) e.setNotaria(notaria);

        String tomo = leerTextoOpcional("Nuevo tomo (ENTER para mantener): ").trim();
        if (!tomo.isEmpty()) e.setTomo(tomo);

        String folio = leerTextoOpcional("Nuevo folio (ENTER para mantener): ").trim();
        if (!folio.isEmpty()) e.setFolio(folio);

        String obs = leerTextoOpcional("Nuevas observaciones (ENTER para mantener / escribir '-' para NULL): ").trim();
        if (!obs.isEmpty()) {
            if (obs.equals("-")) e.setObservaciones(null);
            else e.setObservaciones(obs);
        }

        String propIdTxt = leerTextoOpcional("Nuevo ID de propiedad asociada (ENTER para mantener): ").trim();
        if (!propIdTxt.isEmpty()) {
            try {
                long nuevoPropId = Long.parseLong(propIdTxt);
                e.setPropiedadId(nuevoPropId); // FK UNIQUE (1:1)
            } catch (NumberFormatException nfe) {
                System.out.println("ID de propiedad inválido. Se mantiene el actual.");
            }
        }

        try {
            escrituraService.actualizar(e);
            System.out.println("Escritura actualizada correctamente.");
        } catch (exceptions.ValidationException ve) {
            System.out.println("Datos inválidos: " + ve.getMessage());
        } catch (exceptions.IntegrityException ie) {
            System.out.println("Integridad: " + ie.getMessage());
        } catch (exceptions.DatabaseException de) {
            System.out.println("Error de base de datos: " + de.getMessage());
        } catch (exceptions.AppException ae) {
            System.out.println("Error: " + ae.getMessage());
        }
    }

    
    private void rptPropiedadesCompletasVista() throws AppException {
        var lista = vistasService.propiedadesCompletas();
        if (lista == null || lista.isEmpty()) {
            System.out.println("Sin datos en la vista v_propiedades_completas.");
            return;
        }
        System.out.println("=== Propiedades (vista v_propiedades_completas) ===");
        for (var p : lista) {
            System.out.println(p);
        }
    }

    private void busquedaAvanzadaVista() throws AppException {
        System.out.println("=== Búsqueda avanzada (vista v_busqueda_avanzada) ===");
        String q    = leerTextoOpcional("Texto libre (dirección/padrón/notaría/nro) [ENTER = cualquiera]: ");
        String rSup = leerTextoOpcional("Rango superficie (0-50 | 50-100 | 100-200 | 200+) [ENTER = cualquiera]: ");
        String rAnt = leerTextoOpcional("Rango antigüedad (0-5 | 5-15 | 15-30 | 30+) [ENTER = cualquiera]: ");
        Integer anio = leerEnteroOpcional("Año de escritura (ENTER = cualquiera): ");
        Integer mes  = leerEnteroOpcional("Mes de escritura (1-12, ENTER = cualquiera): ");

        var lista = vistasService.buscarAvanzado(
                emptyToNull(q), emptyToNull(rSup), emptyToNull(rAnt), anio, mes);

        if (lista == null || lista.isEmpty()) {
            System.out.println("Sin resultados con esos filtros.");
            return;
        }
        for (var p : lista) System.out.println(p);
    }

    // =========================
    // Helpers de lectura
    // =========================

    private int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (!scanner.hasNextInt()) {
            System.out.println("Valor inválido. Ingrese un número entero.");
            scanner.nextLine();
            System.out.print(mensaje);
        }
        int valor = scanner.nextInt();
        scanner.nextLine(); // limpia salto de línea
        return valor;
    }

    private long leerLong(String mensaje) {
        System.out.print(mensaje);
        while (!scanner.hasNextLong()) {
            System.out.println("Valor inválido. Ingrese un número entero largo.");
            scanner.nextLine();
            System.out.print(mensaje);
        }
        long valor = scanner.nextLong();
        scanner.nextLine();
        return valor;
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private String leerTextoOpcional(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }
    
    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private Integer leerEnteroOpcional(String mensaje) {
        System.out.print(mensaje);
        String txt = scanner.nextLine().trim();
        if (txt.isEmpty()) return null;
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, se ignora (queda como cualquiera).");
            return null;
        }
    }
}
