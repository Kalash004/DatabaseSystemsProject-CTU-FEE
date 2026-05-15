package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "provedeni_ukonu")
public class ProvedeniUkonu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provedeni_ukonu_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "fk_pacient_id", nullable = false)
    private Pacient fkPacient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "fk_doktor_id", nullable = false)
    private Doktor fkDoktor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "fk_ukon_id", nullable = false)
    private Ukon fkUkon;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;

    @Column(name = "cas", nullable = false)
    private LocalTime cas;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pacient getFkPacient() {
        return fkPacient;
    }

    public void setFkPacient(Pacient fkPacient) {
        this.fkPacient = fkPacient;
    }

    public Doktor getFkDoktor() {
        return fkDoktor;
    }

    public void setFkDoktor(Doktor fkDoktor) {
        this.fkDoktor = fkDoktor;
    }

    public Ukon getFkUkon() {
        return fkUkon;
    }

    public void setFkUkon(Ukon fkUkon) {
        this.fkUkon = fkUkon;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getCas() {
        return cas;
    }

    public void setCas(LocalTime cas) {
        this.cas = cas;
    }

}