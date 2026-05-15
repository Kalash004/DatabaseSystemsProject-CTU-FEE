package entities;

import entities.converters.DulezitostLuzkaConverter;
import entities.converters.StavConverter;
import entities.enums.DulezitostLuzkaEnum;
import entities.enums.StavEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "zdravotni_karta")
public class ZdravotniKarta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zdravotni_karta_id_gen")
    @SequenceGenerator(name = "zdravotni_karta_id_gen", sequenceName = "zdravotni_karta_zdravotni_karta_id_seq", allocationSize = 1)
    @Column(name = "zdravotni_karta_id", nullable = false)
    private Integer id;

    @Column(name = "cislo_karty", nullable = false, length = 20)
    private String cisloKarty;

    @Column(name = "stav", columnDefinition = "stav_enum")
    @Convert(converter = StavConverter.class)
    private StavEnum stav;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "datum_zalozeni", nullable = false)
    private LocalDate datumZalozeni;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCisloKarty() {
        return cisloKarty;
    }

    public void setCisloKarty(String cisloKarty) {
        this.cisloKarty = cisloKarty;
    }

    public StavEnum getStav() {
        return stav;
    }

    public void setStav(StavEnum stav) {
        this.stav = stav;
    }

    public LocalDate getDatumZalozeni() {
        return datumZalozeni;
    }

    public void setDatumZalozeni(LocalDate datumZalozeni) {
        this.datumZalozeni = datumZalozeni;
    }
}