package DAO;

import entities.Mistnost;
import entities.enums.OddeleniEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class MistnostDAO extends BaseDAO<Mistnost, Integer> {

    public MistnostDAO(EntityManager entityManager) {
        super(entityManager, Mistnost.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     *  Finds all rooms in a specific department
     */
    public List<Mistnost> selectByOddeleni(OddeleniEnum oddeleni) {
        TypedQuery<Mistnost> query = entityManager.createQuery(
                "SELECT m FROM Mistnost m WHERE m.oddeleni = :dept", Mistnost.class
        );
        query.setParameter("dept", oddeleni);
        return query.getResultList();
    }
}