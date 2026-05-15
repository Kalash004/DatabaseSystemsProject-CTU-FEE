package DAO;

import entities.Lek;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class LekDAO extends BaseDAO<Lek, Integer> {

    public LekDAO(EntityManager entityManager) {
        super(entityManager, Lek.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Finds a medicine by its exact name.
     */
    public Lek selectByNazev(String nazev) {
        TypedQuery<Lek> query = entityManager.createQuery(
                "SELECT l FROM Lek l WHERE l.nazevLeku = :nazev", Lek.class
        );
        query.setParameter("nazev", nazev);
        return query.getResultStream().findFirst().orElse(null);
    }

    /**
     * Finds medicines containing a specific string (case-insensitive).
     */
    public List<Lek> searchByNazev(String partialNazev) {
        TypedQuery<Lek> query = entityManager.createQuery(
                "SELECT l FROM Lek l WHERE LOWER(l.nazevLeku) LIKE LOWER(:name)", Lek.class
        );
        query.setParameter("name", "%" + partialNazev + "%");
        return query.getResultList();
    }
}