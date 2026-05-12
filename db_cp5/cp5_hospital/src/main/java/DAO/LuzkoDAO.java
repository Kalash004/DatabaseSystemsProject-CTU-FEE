package DAO;

import entities.Luzko;
import entities.Mistnost;
import entities.enums.DulezitostLuzkaEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class LuzkoDAO extends BaseDAO<Luzko, Integer> {

    public LuzkoDAO(EntityManager entityManager) {
        super(entityManager, Luzko.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Finds all beds located in a specific room.
     */
    public List<Luzko> selectByMistnost(Mistnost mistnost) {
        TypedQuery<Luzko> query = entityManager.createQuery(
                "SELECT l FROM Luzko l WHERE l.fkMistnost = :mistnost", Luzko.class
        );
        query.setParameter("mistnost", mistnost);
        return query.getResultList();
    }

    /**
     * Finds all beds of a certain importance level (e.g., JIP/ICU).
     */
    public List<Luzko> selectByDulezitost(DulezitostLuzkaEnum dulezitost) {
        TypedQuery<Luzko> query = entityManager.createQuery(
                "SELECT l FROM Luzko l WHERE l.dulezitostLuzka = :dulezitost", Luzko.class
        );
        query.setParameter("dulezitost", dulezitost);
        return query.getResultList();
    }
}