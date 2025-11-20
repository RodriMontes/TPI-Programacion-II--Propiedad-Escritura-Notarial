/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.time.LocalDate;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class EscrituraNotarial extends BaseEntity {

    private String nroEscritura;
    private LocalDate fecha;
    private String notaria;
    private String tomo;
    private String folio;
    private String observaciones;

    // FK a propiedad (esn_propiedad_id)
    private Long propiedadId;

    public EscrituraNotarial() {
        super();
    }

    public EscrituraNotarial(Long id, boolean eliminado,
                             String nroEscritura, LocalDate fecha,
                             String notaria, String tomo,
                             String folio, String observaciones,
                             Long propiedadId) {
        super(id, eliminado);
        this.nroEscritura = nroEscritura;
        this.fecha = fecha;
        this.notaria = notaria;
        this.tomo = tomo;
        this.folio = folio;
        this.observaciones = observaciones;
        this.propiedadId = propiedadId;
    }

    public String getNroEscritura() {
        return nroEscritura;
    }

    public void setNroEscritura(String nroEscritura) {
        this.nroEscritura = nroEscritura;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNotaria() {
        return notaria;
    }

    public void setNotaria(String notaria) {
        this.notaria = notaria;
    }

    public String getTomo() {
        return tomo;
    }

    public void setTomo(String tomo) {
        this.tomo = tomo;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getPropiedadId() {
        return propiedadId;
    }

    public void setPropiedadId(Long propiedadId) {
        this.propiedadId = propiedadId;
    }

    @Override
    public String toString() {
        return "EscrituraNotarial{" +
                "id=" + id +
                ", eliminado=" + eliminado +
                ", nroEscritura='" + nroEscritura + '\'' +
                ", fecha=" + fecha +
                ", notaria='" + notaria + '\'' +
                ", tomo='" + tomo + '\'' +
                ", folio='" + folio + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", propiedadId=" + propiedadId +
                '}';
    }
}
