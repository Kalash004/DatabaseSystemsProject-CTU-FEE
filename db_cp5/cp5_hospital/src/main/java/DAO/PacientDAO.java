package DAO;

import entities.Pacient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class PacientDAO extends BaseDAO<Pacient, Integer> {

    public PacientDAO(EntityManager entityManager) {
        super(entityManager, Pacient.class);
    }

    /**
     * Finds patients by their blood type (e.g., "A+", "B-").
     */
    public List<Pacient> selectByKrevniSkupina(String krevniSkupina) {
        TypedQuery<Pacient> query = entityManager.createQuery(
                "SELECT p FROM Pacient p WHERE p.krevniSkupina = :skupina", Pacient.class
        );
        query.setParameter("skupina", krevniSkupina);
        return query.getResultList();
    }
}