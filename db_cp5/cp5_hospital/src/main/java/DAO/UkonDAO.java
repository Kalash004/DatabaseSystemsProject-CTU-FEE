package DAO;

import entities.Ukon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class UkonDAO extends BaseDAO<Ukon, Integer> {

    public UkonDAO(EntityManager entityManager) {
        super(entityManager, Ukon.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Finds medical procedures by name keywords.
     */
    public List<Ukon> selectByNazev(String keyword) {
        TypedQuery<Ukon> query = entityManager.createQuery(
                "SELECT u FROM Ukon u WHERE LOWER(u.nazevUkonu) LIKE LOWER(:keyword)", Ukon.class
        );
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }
}