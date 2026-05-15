package entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "ukon")
public class Ukon {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ukon_id_gen")
    @SequenceGenerator(name = "ukon_id_gen", sequenceName = "ukon_ukon_id_seq", allocationSize = 1)
    @Column(name = "ukon_id", nullable = false)
    private Integer id;

    @Column(name = "nazev_ukonu", nullable = false, length = 100)
    private String nazevUkonu;

    @Column(name = "popis_ukonu", nullable = false, length = Integer.MAX_VALUE)
    private String popisUkonu;

    @ManyToMany
    @JoinTable(
            name = "registrovane_leky_pro_ukon",
            joinColumns = @JoinColumn(name = "ukon_id"),
            inverseJoinColumns = @JoinColumn(name = "lek_id")
    )
    private Set<Lek> registrovaneLeky = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "kvalifikace")
    private Set<Doktor> kvalifikovaniDoktori = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNazevUkonu() {
        return nazevUkonu;
    }

    public void setNazevUkonu(String nazevUkonu) {
        this.nazevUkonu = nazevUkonu;
    }

    public String getPopisUkonu() {
        return popisUkonu;
    }

    public void setPopisUkonu(String popisUkonu) {
        this.popisUkonu = popisUkonu;
    }

    public Set<Lek> getRegistrovaneLeky() {
        return registrovaneLeky;
    }

    public void setRegistrovaneLeky(Set<Lek> registrovaneLeky) {
        this.registrovaneLeky = registrovaneLeky;
    }

    public Set<Doktor> getKvalifikovaniDoktori() {
        return kvalifikovaniDoktori;
    }

    public void setKvalifikovaniDoktori(Set<Doktor> kvalifikovaniDoktori) {
        this.kvalifikovaniDoktori = kvalifikovaniDoktori;
    }

}