package DAO;

import entities.JeZapsanDoLuzka;
import entities.Pacient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class JeZapsanDoLuzkaDAO extends BaseDAO<JeZapsanDoLuzka, Integer> {

    public JeZapsanDoLuzkaDAO(EntityManager entityManager) {
        super(entityManager, JeZapsanDoLuzka.class);
    }

    /**
     * Finds all bed registrations for a specific patient.
     */
    public List<JeZapsanDoLuzka> selectByPacient(Pacient pacient) {
        TypedQuery<JeZapsanDoLuzka> query = entityManager.createQuery(
                "SELECT j FROM JeZapsanDoLuzka j WHERE j.fkPacient = :pacient", JeZapsanDoLuzka.class
        );
        query.setParameter("pacient", pacient);
        return query.getResultList();
    }

    /**
     * Finds active bed registration for a specific bed (where datumDo is NULL).
     */
    public List<JeZapsanDoLuzka> selectActiveByLuzko(entities.Luzko luzko) {
        return entityManager.createQuery(
                "SELECT j FROM JeZapsanDoLuzka j WHERE j.fkLuzko = :luzko AND j.datumDo IS NULL AND j.datumOd IS NOT NULL ", JeZapsanDoLuzka.class)
                .setParameter("luzko", luzko)
                .getResultList();
    }

    /**
     * Finds active bed registration for a specific patient (where datumDo is NULL).
     */
    public List<JeZapsanDoLuzka> selectActiveByPacient(Pacient pacient) {
        return entityManager.createQuery(
                "SELECT j FROM JeZapsanDoLuzka j WHERE j.fkPacient = :pacient AND j.datumDo IS NULL", JeZapsanDoLuzka.class)
                .setParameter("pacient", pacient)
                .getResultList();
    }
}
