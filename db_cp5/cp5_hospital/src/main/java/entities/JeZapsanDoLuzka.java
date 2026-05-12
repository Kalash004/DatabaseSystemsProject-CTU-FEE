package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "je_zapsan_do_luzka")
public class JeZapsanDoLuzka {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "je_zapsan_do_luzka_id_gen")
    @SequenceGenerator(name = "je_zapsan_do_luzka_id_gen", sequenceName = "je_zapsan_do_luzka_je_zapsan_do_luzka_id_seq", allocationSize = 1)
    @Column(name = "je_zapsan_do_luzka_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_pacient_id", nullable = false)
    private Pacient fkPacient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_luzko_id", nullable = false)
    private Luzko fkLuzko;

    @Column(name = "datum_od", nullable = false)
    private Instant datumOd;

    @Column(name = "datum_do")
    private Instant datumDo;

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

    public Luzko getFkLuzko() {
        return fkLuzko;
    }

    public void setFkLuzko(Luzko fkLuzko) {
        this.fkLuzko = fkLuzko;
    }

    public Instant getDatumOd() {
        return datumOd;
    }

    public void setDatumOd(Instant datumOd) {
        this.datumOd = datumOd;
    }

    public Instant getDatumDo() {
        return datumDo;
    }

    public void setDatumDo(Instant datumDo) {
        this.datumDo = datumDo;
    }

}