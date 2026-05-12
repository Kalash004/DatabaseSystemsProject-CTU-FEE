package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pacient")
public class Pacient {
    @Id
    @Column(name = "osoba_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "osoba_id", nullable = false)
    private Osoba osoba;

    @Column(name = "krevni_skupina", length = 3)
    private String krevniSkupina;

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

    public String getKrevniSkupina() {
        return krevniSkupina;
    }

    public void setKrevniSkupina(String krevniSkupina) {
        this.krevniSkupina = krevniSkupina;
    }

}