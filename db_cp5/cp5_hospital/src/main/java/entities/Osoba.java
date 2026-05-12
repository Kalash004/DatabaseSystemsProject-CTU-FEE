package entities;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "osoba")
public class Osoba {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "osoba_id_gen")
    @SequenceGenerator(name = "osoba_id_gen", sequenceName = "osoba_osoba_id_seq", allocationSize = 1)
    @Column(name = "osoba_id", nullable = false)
    private Integer id;

    @Column(name = "evidencni_cislo_pojistence", nullable = false, length = 10)
    @JdbcTypeCode(Types.CHAR)
    private String evidencniCisloPojistence;

    @Column(name = "jmeno", nullable = false, length = 100)
    private String jmeno;

    @Column(name = "prijmeni", nullable = false, length = 100)
    private String prijmeni;

    @Column(name = "datum_narozeni", nullable = false)
    private LocalDate datumNarozeni;

    @Column(name = "mesto", length = 100)
    private String mesto;

    @Column(name = "ulice", length = 100)
    private String ulice;

    @Column(name = "stat", length = 100)
    private String stat;

    @Column(name = "cislo_nemocnice", length = 20)
    private String cisloNemocnice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEvidencniCisloPojistence() {
        return evidencniCisloPojistence;
    }

    public void setEvidencniCisloPojistence(String evidencniCisloPojistence) {
        this.evidencniCisloPojistence = evidencniCisloPojistence;
    }

    public String getJmeno() {
        return jmeno;
    }

    public void setJmeno(String jmeno) {
        this.jmeno = jmeno;
    }

    public String getPrijmeni() {
        return prijmeni;
    }

    public void setPrijmeni(String prijmeni) {
        this.prijmeni = prijmeni;
    }

    public LocalDate getDatumNarozeni() {
        return datumNarozeni;
    }

    public void setDatumNarozeni(LocalDate datumNarozeni) {
        this.datumNarozeni = datumNarozeni;
    }

    public String getMesto() {
        return mesto;
    }

    public void setMesto(String mesto) {
        this.mesto = mesto;
    }

    public String getUlice() {
        return ulice;
    }

    public void setUlice(String ulice) {
        this.ulice = ulice;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getCisloNemocnice() {
        return cisloNemocnice;
    }

    public void setCisloNemocnice(String cisloNemocnice) {
        this.cisloNemocnice = cisloNemocnice;
    }

}