package entities;

import entities.converters.DulezitostLuzkaConverter;
import entities.enums.DulezitostLuzkaEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "luzko")
public class Luzko {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "luzko_id_gen")
    @SequenceGenerator(name = "luzko_id_gen", sequenceName = "luzko_luzko_id_seq", allocationSize = 1)
    @Column(name = "luzko_id", nullable = false)
    private Integer id;

    @Column(name = "fyzicke_cislo", nullable = false, length = 20)
    private String fyzickeCislo;

    @Column(name = "dulezitost_luzka", columnDefinition = "dulezitost_luzka_enum")
    @Convert(converter = DulezitostLuzkaConverter.class)
    @org.hibernate.annotations.ColumnTransformer(write = "?::dulezitost_luzka_enum")
    private DulezitostLuzkaEnum dulezitostLuzka;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_mistnost_id", nullable = false)
    private Mistnost fkMistnost;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFyzickeCislo() {
        return fyzickeCislo;
    }

    public void setFyzickeCislo(String fyzickeCislo) {
        this.fyzickeCislo = fyzickeCislo;
    }

    public Mistnost getFkMistnost() {
        return fkMistnost;
    }

    public void setFkMistnost(Mistnost fkMistnost) {
        this.fkMistnost = fkMistnost;
    }

    public DulezitostLuzkaEnum getDulezitostLuzka() {
        return dulezitostLuzka;
    }

    public void setDulezitostLuzka(DulezitostLuzkaEnum dulezitostLuzka) {
        this.dulezitostLuzka = dulezitostLuzka;
    }

}