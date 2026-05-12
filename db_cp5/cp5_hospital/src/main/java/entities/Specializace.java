package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "specializace")
public class Specializace {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "specializace_id_gen")
    @SequenceGenerator(name = "specializace_id_gen", sequenceName = "specializace_specializace_id_seq", allocationSize = 1)
    @Column(name = "specializace_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_osoba_id", nullable = false)
    private Doktor fkOsoba;

    @Column(name = "specializace", nullable = false, length = 100)
    private String specializace;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Doktor getFkOsoba() {
        return fkOsoba;
    }

    public void setFkOsoba(Doktor fkOsoba) {
        this.fkOsoba = fkOsoba;
    }

    public String getSpecializace() {
        return specializace;
    }

    public void setSpecializace(String specializace) {
        this.specializace = specializace;
    }

}