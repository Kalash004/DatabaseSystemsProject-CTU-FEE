package DAO;

import entities.Osoba;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class OsobaDAO extends BaseDAO<Osoba, Integer> {

    public OsobaDAO(EntityManager entityManager) {
        super(entityManager, Osoba.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    public Osoba selectByEvidencniCislo(String evidencniCislo) {
        TypedQuery<Osoba> query = entityManager.createQuery(
                "SELECT o FROM Osoba o WHERE o.evidencniCisloPojistence = :cislo", Osoba.class
        );
        query.setParameter("cislo", evidencniCislo);
        // returns null if not found
        return query.getResultStream().findFirst().orElse(null);
    }
}