package entities;

import jakarta.persistence.*;

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

/*
 TODO [Reverse Engineering] create field to map the 'barva_mistnosti' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "barva_mistnosti", columnDefinition = "barva_mistnosti_enum not null")
    private Object barvaMistnosti;
*/
/*
 TODO [Reverse Engineering] create field to map the 'oddeleni' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "oddeleni", columnDefinition = "oddeleni_enum not null")
    private Object oddeleni;
*/
}