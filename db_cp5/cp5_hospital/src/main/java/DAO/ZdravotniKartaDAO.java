package DAO;

import entities.ZdravotniKarta;
import entities.enums.StavEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ZdravotniKartaDAO extends BaseDAO<ZdravotniKarta, Integer> {

    public ZdravotniKartaDAO(EntityManager entityManager) {
        super(entityManager, ZdravotniKarta.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Finds a card by its unique card number (cislo_karty).
     */
    public ZdravotniKarta selectByCisloKarty(String cisloKarty) {
        TypedQuery<ZdravotniKarta> query = entityManager.createQuery(
                "SELECT z FROM ZdravotniKarta z WHERE z.cisloKarty = :cislo", ZdravotniKarta.class
        );
        query.setParameter("cislo", cisloKarty);
        return query.getResultStream().findFirst().orElse(null);
    }

    /**
     * Finds all cards with a specific status (e.g., AKTIVNI).
     */
    public List<ZdravotniKarta> selectByStav(StavEnum stav) {
        TypedQuery<ZdravotniKarta> query = entityManager.createQuery(
                "SELECT z FROM ZdravotniKarta z WHERE z.stav = :stav", ZdravotniKarta.class
        );
        query.setParameter("stav", stav);
        return query.getResultList();
    }
}