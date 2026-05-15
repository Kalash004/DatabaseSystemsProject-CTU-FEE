package DAO;

import entities.ProvedeniUkonu;
import entities.Pacient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ProvedeniUkonuDAO extends BaseDAO<ProvedeniUkonu, Integer> {

    public ProvedeniUkonuDAO(EntityManager entityManager) {
        super(entityManager, ProvedeniUkonu.class);
    }

    /**
     * Finds all procedures performed on a specific patient.
     */
    public List<ProvedeniUkonu> selectByPacient(Pacient pacient) {
        TypedQuery<ProvedeniUkonu> query = entityManager.createQuery(
                "SELECT p FROM ProvedeniUkonu p WHERE p.fkPacient = :pacient", ProvedeniUkonu.class
        );
        query.setParameter("pacient", pacient);
        return query.getResultList();
    }
}
