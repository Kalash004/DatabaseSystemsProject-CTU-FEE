package entities;

import entities.converters.BarvaMistnostiConverter;
import entities.converters.OddeleniConverter;
import entities.enums.BarvaMistnostiEnum;
import entities.enums.OddeleniEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "mistnost")
public class Mistnost {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mistnost_id_gen")
    @SequenceGenerator(name = "mistnost_id_gen", sequenceName = "mistnost_mistnost_id_seq", allocationSize = 1)
    @Column(name = "mistnost_id", nullable = false)
    private Integer id;

    @Column(name = "cislo_mistnosti", nullable = false, length = 20)
    private String cisloMistnosti;

    @Column(name = "barva_mistnosti", columnDefinition = "barva_mistnosti_enum")
    @Convert(converter = BarvaMistnostiConverter.class)
    @org.hibernate.annotations.ColumnTransformer(write = "?::barva_mistnosti_enum")
    private BarvaMistnostiEnum barvaMistnosti;

    @Column(name = "oddeleni", columnDefinition = "oddeleni_enum")
    @Convert(converter = OddeleniConverter.class)
    @org.hibernate.annotations.ColumnTransformer(write = "?::oddeleni_enum")
    private OddeleniEnum oddeleni;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCisloMistnosti() {
        return cisloMistnosti;
    }

    public void setCisloMistnosti(String cisloMistnosti) {
        this.cisloMistnosti = cisloMistnosti;
    }

    public BarvaMistnostiEnum getBarvaMistnosti() {
        return barvaMistnosti;
    }

    public void setBarvaMistnosti(BarvaMistnostiEnum barvaMistnosti) {
        this.barvaMistnosti = barvaMistnosti;
    }

    public OddeleniEnum getOddeleni() {
        return oddeleni;
    }

    public void setOddeleni(OddeleniEnum oddeleni) {
        this.oddeleni = oddeleni;
    }
}