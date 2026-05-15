package DAO;

import entities.Doktor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class DoktorDAO extends BaseDAO<Doktor, Integer> {

    public DoktorDAO(EntityManager entityManager) {
        super(entityManager, Doktor.class);
    }

    // insert(), selectById(), selectAll(), update(), and delete() automatically

    /**
     * Finds a doctor by their CLK registration number.
     */
    public Doktor selectByClk(String clk) {
        TypedQuery<Doktor> query = entityManager.createQuery(
                "SELECT d FROM Doktor d WHERE d.evidencniCisloClk = :clk", Doktor.class
        );
        query.setParameter("clk", clk);
        return query.getResultStream().findFirst().orElse(null);
    }

    /**
     * Retrieves all doctors supervised by a specific doctor.
     */
    public List<Doktor> selectSupervisedBy(Integer supervisorId) {
        Doktor supervisor = selectById(supervisorId);
        if (supervisor != null) {
            // Because of FetchType.LAZY (default for ManyToMany),
            // accessing the list within an open transaction/session loads the data.
            return supervisor.getDohledavani();
        }
        return List.of();
    }

    /**
     * Checks if a doctor with a specific evidencni cislo exists.
     */
    public boolean existsByEvidencniCislo(String evCislo) {
        return entityManager.createQuery("SELECT count(d) FROM Doktor d WHERE d.evidencniCisloPojistence = :ev", Long.class)
                .setParameter("ev", evCislo)
                .getSingleResult() > 0;
    }
}