/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.math.BigDecimal;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class Propiedad extends BaseEntity {

    private String padronCatastral;
    private String direccion;
    private BigDecimal superficieM2;
    private Destino destino;
    private Integer antiguedad;
    private String email; // si querés mapear pro_email

    // Relación 1→1 unidireccional
    private EscrituraNotarial escrituraNotarial;

    public Propiedad() {
        super();
    }

    public Propiedad(Long id, boolean eliminado,
                     String padronCatastral, String direccion,
                     BigDecimal superficieM2, Destino destino,
                     Integer antiguedad, String email,
                     EscrituraNotarial escrituraNotarial) {
        super(id, eliminado);
        this.padronCatastral = padronCatastral;
        this.direccion = direccion;
        this.superficieM2 = superficieM2;
        this.destino = destino;
        this.antiguedad = antiguedad;
        this.email = email;
        this.escrituraNotarial = escrituraNotarial;
    }

    public String getPadronCatastral() {
        return padronCatastral;
    }

    public void setPadronCatastral(String padronCatastral) {
        this.padronCatastral = padronCatastral;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public BigDecimal getSuperficieM2() {
        return superficieM2;
    }

    public void setSuperficieM2(BigDecimal superficieM2) {
        this.superficieM2 = superficieM2;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

    public Integer getAntiguedad() {
        return antiguedad;
    }

    public void setAntiguedad(Integer antiguedad) {
        this.antiguedad = antiguedad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EscrituraNotarial getEscrituraNotarial() {
        return escrituraNotarial;
    }

    public void setEscrituraNotarial(EscrituraNotarial escrituraNotarial) {
        this.escrituraNotarial = escrituraNotarial;
    }

    @Override
    public String toString() {
        return "Propiedad{" +
                "id=" + id +
                ", eliminado=" + eliminado +
                ", padronCatastral='" + padronCatastral + '\'' +
                ", direccion='" + direccion + '\'' +
                ", superficieM2=" + superficieM2 +
                ", destino=" + destino +
                ", antiguedad=" + antiguedad +
                ", email='" + email + '\'' +
                ", escrituraNotarial=" +
                   (escrituraNotarial != null ? escrituraNotarial.getNroEscritura() : "SIN_ESCRITURA") +
                '}';
    }
}
