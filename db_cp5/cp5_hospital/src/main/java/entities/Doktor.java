package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doktor")
public class Doktor {
    @Id
    @Column(name = "osoba_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "osoba_id", nullable = false)
    private Osoba osoba;

    @Column(name = "icl", nullable = false, length = 6)
    @JdbcTypeCode(Types.CHAR)
    private String icl;

    @Column(name = "evidencni_cislo_clk", nullable = false, length = 6)
    @JdbcTypeCode(Types.CHAR)
    private String evidencniCisloClk;

    @Column(name = "identifikator_nrzp", nullable = false, length = 9)
    @JdbcTypeCode(Types.CHAR)
    private String identifikatorNrzp;

    public List<Doktor> getDohledavani() {
        return dohledavani;
    }

    public void setDohledavani(List<Doktor> dohledavani) {
        this.dohledavani = dohledavani;
    }

    public List<Doktor> getDohledavaci() {
        return dohledavaci;
    }

    public void setDohledavaci(List<Doktor> dohledavaci) {
        this.dohledavaci = dohledavaci;
    }

    @ManyToMany
    @JoinTable(
            name = "dohledani",
            joinColumns = @JoinColumn(name = "dohledavaci_osoba_id"),
            inverseJoinColumns = @JoinColumn(name = "dohledavany_osoba_id")
    )
    private List<Doktor> dohledavani = new ArrayList<>(); // doctors this doctor supervises

    @ManyToMany(mappedBy = "dohledavani")
    private List<Doktor> dohledavaci = new ArrayList<>(); // doctors who supervise this doctor

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Osoba getOsoba() {
        return osoba;
    }

    public void setOsoba(Osoba osoba) {
        this.osoba = osoba;
    }

    public String getIcl() {
        return icl;
    }

    public void setIcl(String icl) {
        this.icl = icl;
    }

    public String getEvidencniCisloClk() {
        return evidencniCisloClk;
    }

    public void setEvidencniCisloClk(String evidencniCisloClk) {
        this.evidencniCisloClk = evidencniCisloClk;
    }

    public String getIdentifikatorNrzp() {
        return identifikatorNrzp;
    }

    public void setIdentifikatorNrzp(String identifikatorNrzp) {
        this.identifikatorNrzp = identifikatorNrzp;
    }

}