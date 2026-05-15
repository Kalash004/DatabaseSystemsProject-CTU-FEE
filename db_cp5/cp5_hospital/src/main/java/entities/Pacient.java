package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pacient")
@PrimaryKeyJoinColumn(name = "osoba_id")
public class Pacient extends Osoba {

    @Column(name = "krevni_skupina", length = 3)
    private String krevniSkupina;

    @OneToMany(mappedBy = "fkPacient")
    private Set<JeZapsanDoLuzka> jeZapsanDoLuzkas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "fkPacient")
    private Set<ProvedeniUkonu> provedeniUkonus = new LinkedHashSet<>();

    @OneToOne
    @JoinTable(name = "vlastni", joinColumns = {@JoinColumn(name = "osoba_id", unique = true)}, inverseJoinColumns = {@JoinColumn(name = "zdravotni_karta_id", unique = true)})
    private ZdravotniKarta zdravotniKarta;



    public String getKrevniSkupina() {
        return krevniSkupina;
    }

    public void setKrevniSkupina(String krevniSkupina) {
        this.krevniSkupina = krevniSkupina;
    }

    public Set<JeZapsanDoLuzka> getJeZapsanDoLuzkas() {
        return jeZapsanDoLuzkas;
    }

    public void setJeZapsanDoLuzkas(Set<JeZapsanDoLuzka> jeZapsanDoLuzkas) {
        this.jeZapsanDoLuzkas = jeZapsanDoLuzkas;
    }

    public Set<ProvedeniUkonu> getProvedeniUkonus() {
        return provedeniUkonus;
    }

    public void setProvedeniUkonus(Set<ProvedeniUkonu> provedeniUkonus) {
        this.provedeniUkonus = provedeniUkonus;
    }

    public ZdravotniKarta getZdravotniKarta() {
        return zdravotniKarta;
    }

    public void setZdravotniKarta(ZdravotniKarta zdravotniKarta) {
        this.zdravotniKarta = zdravotniKarta;
    }

}