package services;

import DAO.*;
import entities.*;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HospitalService {

    private EntityManager em;
    private PacientDAO pacientDAO;
    private JeZapsanDoLuzkaDAO jeZapsanDAO;
    private ZdravotniKartaDAO zdravotniKartaDAO;
    private UkonDAO ukonDAO;
    private DoktorDAO doktorDAO;
    private LuzkoDAO luzkoDAO;
    private LekDAO lekDAO;
    private ProvedeniUkonuDAO provedeniUkonuDAO;

    public HospitalService(EntityManager em) {
        this.em = em;
        this.pacientDAO = new PacientDAO(em);
        this.jeZapsanDAO = new JeZapsanDoLuzkaDAO(em);
        this.zdravotniKartaDAO = new ZdravotniKartaDAO(em);
        this.ukonDAO = new UkonDAO(em);
        this.doktorDAO = new DoktorDAO(em);
        this.luzkoDAO = new LuzkoDAO(em);
        this.lekDAO = new LekDAO(em);
        this.provedeniUkonuDAO = new ProvedeniUkonuDAO(em);
    }

    /**
     * Helper method to wrap operations in a transaction (Lambda / Functional style)
     * Accepts action
     * Begins transaction
     * Does the action
     * Commits the action
     * If database returns exception, rollbacks and throws error
     */
    private <T> T inTransaction(java.util.function.Function<EntityManager, T> action) {
        em.getTransaction().begin();
        try {
            T result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Task 1
     * Transactional operation (create patient and register to a bed)
     * Finds specific bed based on physical number.
     * Gets all current uses of the bed
     * Finds existing patient or registers a new one
     *  if new patient -> creates new zdravKart. joines them in 1:1
     * Creates a local new zapis
     * Inserts the local zapis into database
     * This whole process is wrapped into inTransaction
     */
    public Pacient admitNewPatientToBed(String evCislo, String jmeno, String prijmeni, LocalDate datumNarozeni,
            String fyzickeCisloLuzka) {
        return inTransaction(entityManager -> {

            // Find specific bed based on physical number
            Luzko luzko = java.util.Optional.ofNullable(luzkoDAO.selectByFyzickeCislo(fyzickeCisloLuzka))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Bed with physical number " + fyzickeCisloLuzka + " does not exist!"));

            // Is the bed available?
            List<JeZapsanDoLuzka> existujiciZapisy = jeZapsanDAO.selectActiveByLuzko(luzko);

            if (!existujiciZapisy.isEmpty()) {
                throw new IllegalStateException("Bed " + luzko.getFyzickeCislo() + " is already occupied!");
            }

            // Find or create patient and their medical card using orElseGet
            Pacient pacient = java.util.Optional.ofNullable(pacientDAO.selectByEvidencniCislo(evCislo))
                    .orElseGet(() -> {
                        Pacient novyPacient = new Pacient();
                        novyPacient.setEvidencniCisloPojistence(evCislo);
                        novyPacient.setJmeno(jmeno);
                        novyPacient.setPrijmeni(prijmeni);
                        novyPacient.setDatumNarozeni(datumNarozeni);
                        pacientDAO.insert(novyPacient);

                        ZdravotniKarta karta = new ZdravotniKarta();
                        karta.setCisloKarty("ZK-" + evCislo);
                        karta.setDatumZalozeni(LocalDate.now());
                        karta.setStav(entities.enums.StavEnum.AKTIVNI);
                        zdravotniKartaDAO.insert(karta);

                        novyPacient.setZdravotniKarta(karta);
                        pacientDAO.update(novyPacient);
                        return novyPacient;
                    });

            // Work with another entity (JeZapsanDoLuzka - Bed Registration)
            JeZapsanDoLuzka zapis = new JeZapsanDoLuzka();
            zapis.setFkPacient(pacient);
            zapis.setFkLuzko(luzko);
            zapis.setDatumOd(Instant.now());
            jeZapsanDAO.insert(zapis);

            return pacient;
        });
    }

    /**
     * Alternative: 1. CP-4 Transaction operation exactly as requested
     */
    public Void executeCp4Transaction(String evidencniCisloPacienta, String fyzickeCisloLuzka) {
        return inTransaction(entityManager -> {
            Pacient pacient = java.util.Optional.ofNullable(pacientDAO.selectByEvidencniCislo(evidencniCisloPacienta))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found!"));

            Luzko luzko = java.util.Optional.ofNullable(luzkoDAO.selectByFyzickeCislo(fyzickeCisloLuzka))
                    .orElseThrow(() -> new IllegalArgumentException("Bed not found!"));

            // 1. Attempt to update card status to 'inactive'
            ZdravotniKarta karta = pacient.getZdravotniKarta();
            if (karta != null) {
                karta.setStav(entities.enums.StavEnum.NEAKTIVNI);
                zdravotniKartaDAO.update(karta);
            }

            // 2. Register patient to the bed
            JeZapsanDoLuzka zapis = new JeZapsanDoLuzka();
            zapis.setFkPacient(pacient);
            zapis.setFkLuzko(luzko);
            zapis.setDatumOd(Instant.now());
            jeZapsanDAO.insert(zapis);

            // 3. Manual rollback of all changes within the transaction
            System.out.println("Performing manual ROLLBACK according to CP-4 requirements...");
            entityManager.getTransaction().rollback();

            return null;
        });
    }

    /**
     * 2. Complex INSERT with N:M relationships
     */
    public Ukon createProcedureWithMedications(String nazevUkonu, String popisUkonu, List<String> nazvyLeku) {
        return inTransaction(entityManager -> {

            // Convert list of medication names to entities using Optional and orElseGet
            Set<Lek> leky = nazvyLeku.stream().map(nazev -> {
                return java.util.Optional.ofNullable(lekDAO.selectByNazev(nazev))
                        .orElseGet(() -> {
                            Lek novyLek = new Lek();
                            novyLek.setNazevLeku(nazev);
                            lekDAO.insert(novyLek);
                            return novyLek;
                        });
            }).collect(Collectors.toSet());

            Ukon novyUkon = new Ukon();
            novyUkon.setNazevUkonu(nazevUkonu);
            novyUkon.setPopisUkonu(popisUkonu);

            // Set N:M relationship
            novyUkon.setRegistrovaneLeky(leky);

            ukonDAO.insert(novyUkon);
            return novyUkon;
        });
    }

    /**
     * 3. INSERT utilizing inheritance
     */
    public Doktor registerNewDoctor(String evCislo, String jmeno, String prijmeni, LocalDate narozeni, String icl,
            String evClk, String nrzp) {
        return inTransaction(entityManager -> {

            boolean doctorExists = doktorDAO.existsByEvidencniCislo(evCislo);

            if (doctorExists) {
                throw new IllegalStateException("Doctor with this registration number already exists!");
            }

            Doktor d = new Doktor();
            d.setEvidencniCisloPojistence(evCislo); // Property from Osoba (Parent)
            d.setJmeno(jmeno); // Property from Osoba (Parent)
            d.setPrijmeni(prijmeni); // Property from Osoba (Parent)
            d.setDatumNarozeni(narozeni); // Property from Osoba (Parent)

            d.setIcl(icl); // Property from Doktor (Child)
            d.setEvidencniCisloClk(evClk); // Property from Doktor (Child)
            d.setIdentifikatorNrzp(nrzp); // Property from Doktor (Child)

            doktorDAO.insert(d);
            return d;
        });
    }

    /**
     * 4. UPDATE related data
     */
    public JeZapsanDoLuzka transferPatientToNewBed(String evidencniCisloPacienta, String noveFyzickeCisloLuzka) {
        return inTransaction(entityManager -> {
            Pacient pacient = java.util.Optional.ofNullable(pacientDAO.selectByEvidencniCislo(evidencniCisloPacienta))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found!"));

            Luzko noveLuzko = java.util.Optional.ofNullable(luzkoDAO.selectByFyzickeCislo(noveFyzickeCisloLuzka))
                    .orElseThrow(() -> new IllegalArgumentException("New bed not found!"));

            List<JeZapsanDoLuzka> aktivniZapisy = jeZapsanDAO.selectActiveByPacient(pacient);

            if (aktivniZapisy.isEmpty()) {
                throw new IllegalStateException("Patient is not registered to any bed.");
            }

            JeZapsanDoLuzka staryZapis = aktivniZapisy.get(0);
            staryZapis.setDatumDo(Instant.now());
            jeZapsanDAO.update(staryZapis);

            JeZapsanDoLuzka novyZapis = new JeZapsanDoLuzka();
            novyZapis.setFkPacient(pacient);
            novyZapis.setFkLuzko(noveLuzko);
            novyZapis.setDatumOd(Instant.now());
            jeZapsanDAO.insert(novyZapis);

            if (noveLuzko.getDulezitostLuzka() == entities.enums.DulezitostLuzkaEnum.JEDNOTKA_INTENZIVNI_PECE) {
                ZdravotniKarta karta = pacient.getZdravotniKarta();
                if (karta != null) {
                    karta.setStav(entities.enums.StavEnum.AKTIVNI);
                    zdravotniKartaDAO.update(karta);
                }
            }

            return novyZapis;
        });
    }

    /**
     * 5. DELETE operation
     */
    public Void removePatientSafely(String evidencniCisloPacienta) {
        return inTransaction(entityManager -> {
            Pacient pacient = java.util.Optional.ofNullable(pacientDAO.selectByEvidencniCislo(evidencniCisloPacienta))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found!"));

            List<ProvedeniUkonu> ukony = provedeniUkonuDAO.selectByPacient(pacient);

            if (!ukony.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot delete patient because they have existing performed procedures (RESTRICT).");
            }

            // 1. Delete associated JeZapsanDoLuzka records to satisfy Hibernate memory state
            List<JeZapsanDoLuzka> zapisy = jeZapsanDAO.selectByPacient(pacient);
            for (JeZapsanDoLuzka z : zapisy) {
                jeZapsanDAO.delete(z);
            }

            // 2. Delete the associated ZdravotniKarta (otherwise it becomes an orphan in the database)
            ZdravotniKarta karta = pacient.getZdravotniKarta();
            if (karta != null) {
                // Unlink first to avoid constraint issues during flush
                pacient.setZdravotniKarta(null); 
                zdravotniKartaDAO.delete(karta);
            }

            // 3. Safely delete the patient
            pacientDAO.delete(pacient);
            return null;
        });
    }
}
