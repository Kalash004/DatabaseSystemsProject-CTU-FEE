package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "doktor")
@PrimaryKeyJoinColumn(name = "osoba_id")
public class Doktor extends Osoba {

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

    @ManyToMany
    @JoinTable(
            name = "kvalifikace_doktora",
            joinColumns = @JoinColumn(name = "doktor_id"),
            inverseJoinColumns = @JoinColumn(name = "ukon_id")
    )
    private Set<Ukon> kvalifikace = new LinkedHashSet<>();



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

    public Set<Ukon> getKvalifikace() {
        return kvalifikace;
    }

    public void setKvalifikace(Set<Ukon> kvalifikace) {
        this.kvalifikace = kvalifikace;
    }

}