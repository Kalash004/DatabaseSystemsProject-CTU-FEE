package entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "lek")
public class Lek {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lek_id_gen")
    @SequenceGenerator(name = "lek_id_gen", sequenceName = "lek_lek_id_seq", allocationSize = 1)
    @Column(name = "lek_id", nullable = false)
    private Integer id;

    @Column(name = "nazev_leku", nullable = false, length = 100)
    private String nazevLeku;

    @ManyToMany(mappedBy = "registrovaneLeky")
    private Set<Ukon> ukony = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNazevLeku() {
        return nazevLeku;
    }

    public void setNazevLeku(String nazevLeku) {
        this.nazevLeku = nazevLeku;
    }

    public Set<Ukon> getUkony() {
        return ukony;
    }

    public void setUkony(Set<Ukon> ukony) {
        this.ukony = ukony;
    }

}