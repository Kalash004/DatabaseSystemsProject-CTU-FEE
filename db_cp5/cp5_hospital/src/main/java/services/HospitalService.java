package services;

import DAO.*;
import entities.*;
import entities.enums.StavEnum;
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
     * if new patient -> creates new zdravKart. joines them in 1:1
     * Creates a local new zapis
     * Inserts the local zapis into database
     * This whole process is wrapped into inTransaction
     */
    public Pacient admitPatientToBed(String evCislo, String jmeno, String prijmeni, LocalDate datumNarozeni,
                                     String fyzickeCisloLuzka, boolean throwError) {
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
            if (throwError) {
                throw new RuntimeException("Testing exception");
            }

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
     * Alternative: Task 1. CP-4 Transaction
     * Attemps to update card to inactive
     * Registers patient to a bed
     * And rolls back the whole transaction
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
     * Task 2 Complex INSERT with N:M relationships
     * Finds medication in database, if it does not exist, creats a new one and isnerts into the database
     * Registers all the medications for the action
     * Inserts the new action and medication registration into database
     */
    public Ukon createProcedureWithMedications(String nazevUkonu, String popisUkonu, List<String> nazvyLeku) {
        return inTransaction(entityManager -> {

            // Finds medication in database, if it does not exist, creats a new one and isnerts into the database
            Set<Lek> leky = nazvyLeku.stream().map(nazev -> {
                return java.util.Optional.ofNullable(lekDAO.selectByNazev(nazev))
                        .orElseGet(() -> {
                            Lek novyLek = new Lek();
                            novyLek.setNazevLeku(nazev);
                            lekDAO.insert(novyLek);
                            return novyLek;
                        });
            }).collect(Collectors.toSet());

            // Creates new action, sets name and description
            Ukon novyUkon = new Ukon();
            novyUkon.setNazevUkonu(nazevUkonu);
            novyUkon.setPopisUkonu(popisUkonu);

            // Registers all the medications for the action
            // Set N:M relationship
            novyUkon.setRegistrovaneLeky(leky);

            // Inserts the new action and medication registration into database
            ukonDAO.insert(novyUkon);
            return novyUkon;
        });
    }

    /**
     * Exists only for testing purpouses dont grade (wont be called during presentation)
     * Deletes a procedure by name and also deletes its associated medications
     * if they are not used by any other procedure.
     */
    public Void deleteProcedureWithMedications(String nazevUkonu) {
        return inTransaction(entityManager -> {
            List<Ukon> ukony = entityManager.createQuery("SELECT u FROM Ukon u WHERE u.nazevUkonu = :nazev", Ukon.class)
                    .setParameter("nazev", nazevUkonu)
                    .getResultList();

            for (Ukon u : ukony) {
                // Create a copy so we can iterate over them after clearing the collection
                Set<Lek> leky = new java.util.HashSet<>(u.getRegistrovaneLeky());

                // Clear the relationship from the owning side to delete join table (registration) records
                u.getRegistrovaneLeky().clear();
                ukonDAO.delete(u);

                // Remove orphaned medications (Lek)
                for (Lek lek : leky) {
                    Long count = entityManager.createQuery("SELECT COUNT(u) FROM Ukon u JOIN u.registrovaneLeky l WHERE l = :lek", Long.class)
                            .setParameter("lek", lek)
                            .getSingleResult();
                    if (count == 0) {
                        lekDAO.delete(lek);
                    }
                }
            }
            return null;
        });
    }

    /**
     * Task 3. INSERT utilizing inheritance
     * Checks if doctor with same evidencni cislo exists, if yes throws exception
     * If not creates a new local doctor with correct properties from input
     * Inserts doktor and osoba to the database via doktorDAO
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
     * Exists only for testing purpouses dont grade (wont be called during presentation)
     * Deletes a doctor by their registration number safely.
     */
    public Void removeDoctorSafely(String evCislo) {
        return inTransaction(entityManager -> {
            Doktor d = java.util.Optional.ofNullable(doktorDAO.selectByEvidencniCislo(evCislo))
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found!"));

            // Clear relationships
            d.getDohledavani().clear();
            d.getDohledavaci().clear();
            d.getKvalifikace().clear();

            doktorDAO.delete(d);
            return null;
        });
    }

    /**
     * Task 4. UPDATE related data
     * Gets patient if exists based on the evidenciCisloPacienta, if not throws exception
     * Gets bed if exists, if not throws exception
     * Check if the new bed is available
     * Finds all current registered beds for this patient
     * If no old registrations exist, throws exception
     * Gets the old registration and ends it by setting DatumDo to current time and updates the registraion to db
     * Creates a new bed registraition and puts to the database
     * Checks if patient has zdravotni karta, and if its status is NEAKTIVNI, sets it to AKTIVNI indicating new registration to bed.
     */
    public JeZapsanDoLuzka transferPatientToNewBed(String evidencniCisloPacienta, String noveFyzickeCisloLuzka) {
        return inTransaction(entityManager -> {
            // Gets patient if exists based on the evidenciCisloPacienta, if not throws exception
            Pacient pacient = java.util.Optional.ofNullable(pacientDAO.selectByEvidencniCislo(evidencniCisloPacienta))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found!"));

            // Gets bed if exists, if not throws exception
            Luzko noveLuzko = java.util.Optional.ofNullable(luzkoDAO.selectByFyzickeCislo(noveFyzickeCisloLuzka))
                    .orElseThrow(() -> new IllegalArgumentException("New bed not found!"));

            // Check if the new bed is available
            List<JeZapsanDoLuzka> existujiciZapisy = jeZapsanDAO.selectActiveByLuzko(noveLuzko);
            if (!existujiciZapisy.isEmpty()) {
                throw new IllegalStateException("Bed " + noveLuzko.getFyzickeCislo() + " is already occupied!");
            }

            // Finds all current registered beds for this patient
            List<JeZapsanDoLuzka> aktivniZapisy = jeZapsanDAO.selectActiveByPacient(pacient);

            // If no registrations exist, throws exception
            if (aktivniZapisy.isEmpty()) {
                throw new IllegalStateException("Patient is not registered to any bed.");
            }

            // Gets the registration and ends it by setting DatumDo to current time and updates the registraion to db
            JeZapsanDoLuzka staryZapis = aktivniZapisy.get(0);
            staryZapis.setDatumDo(Instant.now());
            jeZapsanDAO.update(staryZapis);

            // Creates a new bed registraition and puts to the database
            JeZapsanDoLuzka novyZapis = new JeZapsanDoLuzka();
            novyZapis.setFkPacient(pacient);
            novyZapis.setFkLuzko(noveLuzko);
            novyZapis.setDatumOd(Instant.now());
            jeZapsanDAO.insert(novyZapis);

            // Checks if patient has zdravotni karta, and if its status is NEAKTIVNI, sets it to AKTIVNI indicating new registration to bed.
            ZdravotniKarta karta = pacient.getZdravotniKarta();
            if (karta == null) {
                throw new IllegalStateException("Patient doesnt have the ZdravotniKarta");
            }
            if (karta.getStav() == StavEnum.NEAKTIVNI) {
                karta.setStav(entities.enums.StavEnum.AKTIVNI);
                zdravotniKartaDAO.update(karta);
            }

            return novyZapis;
        });
    }

    /**
     * Task 5. DELETE operation
     * Finds patient based on evidCisloPac, if not exists throws exception
     * If patient has actions on them, we cannot delete them to preserve the action history
     * Deletes associated JeZapsanDoLuzka records
     * Deletes the associated ZdravotniKarta
     * Deletes the patient
     */
    public Void removePatientSafely(String evidencniCisloPacienta) {
        return inTransaction(entityManager -> {
            // Finds patient based on evidCisloPac, if not exists throws exception
            Pacient pacient = java.util.Optional.ofNullable(pacientDAO.selectByEvidencniCislo(evidencniCisloPacienta))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found!"));

            // Finds all the actions on the patient
            List<ProvedeniUkonu> ukony = provedeniUkonuDAO.selectByPacient(pacient);

            // If patient has actions on them, we cannot delete them to preserve the action history
            if (!ukony.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot delete patient because they have existing performed procedures (RESTRICT).");
            }

            // Deletes associated JeZapsanDoLuzka records
            List<JeZapsanDoLuzka> zapisy = jeZapsanDAO.selectByPacient(pacient);
            for (JeZapsanDoLuzka z : zapisy) {
                jeZapsanDAO.delete(z);
            }

            // Deletes the associated ZdravotniKarta
            ZdravotniKarta karta = pacient.getZdravotniKarta();
            if (karta != null) {
                // Unlinks zdravKarta from patient to avoid constraint issues during flush
                pacient.setZdravotniKarta(null);
                zdravotniKartaDAO.delete(karta);
            }

            // Deletes the patient
            pacientDAO.delete(pacient);
            return null;
        });
    }
}
