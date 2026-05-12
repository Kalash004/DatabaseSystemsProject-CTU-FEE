package DAO;

import entities.Chorobopis;
import entities.ZdravotniKarta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ChorobopisDAO extends BaseDAO<Chorobopis, Integer> {

    public ChorobopisDAO(EntityManager entityManager) {
        super(entityManager, Chorobopis.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Finds all medical history entries for a specific health card.
     */
    public List<Chorobopis> selectByZdravotniKarta(ZdravotniKarta karta) {
        TypedQuery<Chorobopis> query = entityManager.createQuery(
                "SELECT c FROM Chorobopis c WHERE c.fkZdravotniKarta = :karta", Chorobopis.class
        );
        query.setParameter("karta", karta);
        return query.getResultList();
    }

    /**
     * Finds entries that are currently "open" (where datum_do is null).
     */
    public List<Chorobopis> selectOpenEntries() {
        TypedQuery<Chorobopis> query = entityManager.createQuery(
                "SELECT c FROM Chorobopis c WHERE c.datumDo IS NULL", Chorobopis.class
        );
        return query.getResultList();
    }
}