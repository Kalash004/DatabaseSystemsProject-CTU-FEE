package DAO;

import entities.Specializace;
import entities.Doktor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class SpecializaceDAO extends BaseDAO<Specializace, Integer> {

    public SpecializaceDAO(EntityManager entityManager) {
        super(entityManager, Specializace.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Retrieves all specializations for a specific doctor.
     */
    public List<Specializace> selectByDoktor(Doktor doktor) {
        TypedQuery<Specializace> query = entityManager.createQuery(
                "SELECT s FROM Specializace s WHERE s.fkOsoba = :doktor", Specializace.class
        );
        query.setParameter("doktor", doktor);
        return query.getResultList();
    }

    /**
     * Finds all doctors who have a specific specialization name (e.g., "Kardiologie").
     */
    public List<Specializace> selectByNazev(String nazev) {
        TypedQuery<Specializace> query = entityManager.createQuery(
                "SELECT s FROM Specializace s WHERE LOWER(s.specializace) = LOWER(:nazev)", Specializace.class
        );
        query.setParameter("nazev", nazev);
        return query.getResultList();
    }
}