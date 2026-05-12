package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "chorobopis")
public class Chorobopis {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chorobopis_id_gen")
    @SequenceGenerator(name = "chorobopis_id_gen", sequenceName = "chorobopis_chorobopis_id_seq", allocationSize = 1)
    @Column(name = "chorobopis_id", nullable = false)
    private Integer id;

    @Column(name = "cislo_chorobopisu", nullable = false, length = 20)
    private String cisloChorobopisu;

    @Column(name = "datum_od", nullable = false)
    private LocalDate datumOd;

    @Column(name = "datum_do")
    private LocalDate datumDo;

    @Column(name = "popis_chorobopisu", nullable = false, length = Integer.MAX_VALUE)
    private String popisChorobopisu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_zdravotni_karta_id", nullable = false)
    private ZdravotniKarta fkZdravotniKarta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCisloChorobopisu() {
        return cisloChorobopisu;
    }

    public void setCisloChorobopisu(String cisloChorobopisu) {
        this.cisloChorobopisu = cisloChorobopisu;
    }

    public LocalDate getDatumOd() {
        return datumOd;
    }

    public void setDatumOd(LocalDate datumOd) {
        this.datumOd = datumOd;
    }

    public LocalDate getDatumDo() {
        return datumDo;
    }

    public void setDatumDo(LocalDate datumDo) {
        this.datumDo = datumDo;
    }

    public String getPopisChorobopisu() {
        return popisChorobopisu;
    }

    public void setPopisChorobopisu(String popisChorobopisu) {
        this.popisChorobopisu = popisChorobopisu;
    }

    public ZdravotniKarta getFkZdravotniKarta() {
        return fkZdravotniKarta;
    }

    public void setFkZdravotniKarta(ZdravotniKarta fkZdravotniKarta) {
        this.fkZdravotniKarta = fkZdravotniKarta;
    }

}