import DAO.OsobaDAO;
import entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // SETUP
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        try {
            EntityManager em = emf.createEntityManager();
            try {


//
// --- Testing HospitalService ---
//
//                testStep_1_3_5(em);
//                testStep_2(em);
//                testStep_2_cleanup(em);
//                testStep_3(em);
//                testStep_3_cleanup(em);
//                testStep_4(em);
//                testStep_4_cleanup(em);


                // CLEANUP
            } finally {
                em.close();
            }
        } finally {
            emf.close();
        }
    }

    private static void testStep_1_3_5(EntityManager em) {
        System.out.println("\n=======================================================");
        System.out.println("--- Testing HospitalService (Task 1 & 5: Transactional Insert & Delete) ---");
        System.out.println("=======================================================");

        services.HospitalService hs = new services.HospitalService(em);

        try {
            String testEvCislo = "1231231234";

            // Find an empty bed to avoid IllegalStateException
            List<Luzko> volnaluzka = em.createQuery(
                            "SELECT l FROM Luzko l WHERE l NOT IN " +
                                    "(SELECT j.fkLuzko FROM JeZapsanDoLuzka j WHERE j.datumDo IS NULL)", Luzko.class)
                    .setMaxResults(1)
                    .getResultList();

            if (!volnaluzka.isEmpty()) {
                Luzko vybraneLuzko = volnaluzka.get(0);
                String volneFyzickeCislo = vybraneLuzko.getFyzickeCislo();

                System.out.println("\n[BEFORE ACTION - Checking Database]");

                List<Pacient> predPacienti = em.createQuery("SELECT p FROM Pacient p WHERE p.evidencniCisloPojistence = :ev", Pacient.class)
                        .setParameter("ev", testEvCislo).getResultList();
                System.out.println("Does Patient '1231231234' exist? -> " + (!predPacienti.isEmpty()));

                List<ZdravotniKarta> predKarty = em.createQuery("SELECT k FROM ZdravotniKarta k WHERE k.cisloKarty = :ck", ZdravotniKarta.class)
                        .setParameter("ck", "ZK-" + testEvCislo).getResultList();
                System.out.println("Does Health Card 'ZK-1231231234' exist? -> " + (!predKarty.isEmpty()));

                List<JeZapsanDoLuzka> predZapisy = em.createQuery("SELECT j FROM JeZapsanDoLuzka j WHERE j.fkLuzko = :luzko AND j.datumDo IS NULL", JeZapsanDoLuzka.class)
                        .setParameter("luzko", vybraneLuzko).getResultList();
                System.out.println("Is Bed '" + volneFyzickeCislo + "' occupied? -> " + (!predZapisy.isEmpty()));


                // --------------------------------------------- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                System.out.println("\n[EXECUTING ACTION]");
                System.out.println("Calling hospitalService.admitNewPatientToBed()...");
                hs.admitPatientToBed(
                        testEvCislo,
                        "Karel",
                        "Testovací",
                        LocalDate.of(1980, 1, 1),
                        volneFyzickeCislo,
                        false
                );
                // --------------------------------------------- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                System.out.println("\n[AFTER ACTION - Verifying Database]");

                // Clear persistence context to FORCE a fresh read from the actual database
                em.clear();

                List<Pacient> poPacienti = em.createQuery("SELECT p FROM Pacient p WHERE p.evidencniCisloPojistence = :ev", Pacient.class)
                        .setParameter("ev", testEvCislo).getResultList();
                System.out.println("Does Patient '1231231234' exist? -> " + (!poPacienti.isEmpty()) + " (Name: " + poPacienti.get(0).getJmeno() + " " + poPacienti.get(0).getPrijmeni() + ")");

                List<ZdravotniKarta> poKarty = em.createQuery("SELECT k FROM ZdravotniKarta k WHERE k.cisloKarty = :ck", ZdravotniKarta.class)
                        .setParameter("ck", "ZK-" + testEvCislo).getResultList();
                System.out.println("Does Health Card 'ZK-1231231234' exist? -> " + (!poKarty.isEmpty()) + " (Status: " + poKarty.get(0).getStav() + ")");

                List<JeZapsanDoLuzka> poZapisy = em.createQuery("SELECT j FROM JeZapsanDoLuzka j WHERE j.fkLuzko.id = :luzkoid AND j.datumDo IS NULL", JeZapsanDoLuzka.class)
                        .setParameter("luzkoid", vybraneLuzko.getId()).getResultList();
                System.out.println("Is Bed '" + volneFyzickeCislo + "' occupied? -> " + (!poZapisy.isEmpty()) + " (By Patient: " + poZapisy.get(0).getFkPacient().getJmeno() + ")");

                System.out.println("\nWaiting for 20 seconds before cleanup (check your database now!)...");
                Thread.sleep(20000);

                System.out.println("\n[CLEANUP ACTION - Demonstrating DELETE (Task 5)]");
                System.out.println("Calling hospitalService.removePatientSafely()...");

                // --------------------------------------------- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                hs.removePatientSafely(testEvCislo);
                // --------------------------------------------- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                // Verify cleanup
                em.clear();
                List<Pacient> smazaniPacienti = em.createQuery("SELECT p FROM Pacient p WHERE p.evidencniCisloPojistence = :ev", Pacient.class)
                        .setParameter("ev", testEvCislo).getResultList();
                System.out.println("Does Patient '1231231234' exist after cleanup? -> " + (!smazaniPacienti.isEmpty()));

                List<ZdravotniKarta> smazaneKarty = em.createQuery("SELECT k FROM ZdravotniKarta k WHERE k.cisloKarty = :ck", ZdravotniKarta.class)
                        .setParameter("ck", "ZK-" + testEvCislo).getResultList();
                System.out.println("Does Health Card 'ZK-1231231234' exist after cleanup? -> " + (!smazaneKarty.isEmpty()));

            } else {
                System.out.println("No empty beds available for testing.");
            }
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testStep_2(EntityManager em) {
        services.HospitalService hs = new services.HospitalService(em);
        ArrayList<String> medications = new ArrayList<>(List.of(new String[]{
                "Paracetamol",
                "Ledokain"
        }));
        hs.createProcedureWithMedications("Testing ukon", "This is a test ukon that uses Paracetamol and Ledokain", medications);
        em.clear();
    }

    private static void testStep_2_cleanup(EntityManager em) {
        services.HospitalService hs = new services.HospitalService(em);
        System.out.println("\n[CLEANUP ACTION - Demonstrating DELETE for testStep_2]");
        System.out.println("Calling hospitalService.deleteProcedureWithMedications()...");
        hs.deleteProcedureWithMedications("Testing ukon");
        System.out.println("Cleanup completed.");
        em.clear();
    }

    private static void testStep_3(EntityManager em) {
        services.HospitalService hs = new services.HospitalService(em);
        hs.registerNewDoctor("0000000000", "TestDoktor", "DoktorskyTest", LocalDate.of(1980, 1, 1), "000000", "000000", "000000000");
        em.clear();
    }

    private static void testStep_3_cleanup(EntityManager em) {
        services.HospitalService hs = new services.HospitalService(em);
        System.out.println("\n[CLEANUP ACTION - Demonstrating DELETE for testStep_3]");
        System.out.println("Calling hospitalService.removeDoctorSafely()...");
        hs.removeDoctorSafely("0000000000");
        System.out.println("Cleanup completed.");
        em.clear();
    }

    private static void testStep_4(EntityManager em) {
        System.out.println("\n=======================================================");
        System.out.println("--- Testing HospitalService (Task 4: Transfer Patient) ---");
        System.out.println("=======================================================");

        services.HospitalService hs = new services.HospitalService(em);
        String testEvCislo = "9998887776";


        try {
            // 1. Prepare: Find two empty beds
            List<Luzko> volnaLuzka = em.createQuery(
                            "SELECT l FROM Luzko l WHERE l NOT IN " +
                                    "(SELECT j.fkLuzko FROM JeZapsanDoLuzka j WHERE j.datumDo IS NULL)", Luzko.class)
                    .setMaxResults(2)
                    .getResultList();

            if (volnaLuzka.size() < 2) {
                System.out.println("Not enough free beds for Task 4 test.");
                return;
            }

            Luzko bedA = volnaLuzka.get(0);
            Luzko bedB = volnaLuzka.get(1);

            System.out.println("Admitting patient to Bed A (" + bedA.getFyzickeCislo() + " id: " + bedA.getId() + ")...");
            Pacient pc = hs.admitPatientToBed(testEvCislo, "Tomas", "Prevadeny", LocalDate.of(1990, 5, 5), bedA.getFyzickeCislo(), false);
            System.out.println("ID pacienta: " + pc.getId());

            System.out.println("\nWaiting for 20 seconds before transfer (check your database now!)...");
            Thread.sleep(20000);

            System.out.println("Transferring patient from Bed A to Bed B (" + bedB.getFyzickeCislo() + " id: " + bedB.getId() + ")...");
            hs.transferPatientToNewBed(testEvCislo, bedB.getFyzickeCislo());

            // Verification
            em.clear();
            List<JeZapsanDoLuzka> aktivniZapisy = em.createQuery(
                            "SELECT j FROM JeZapsanDoLuzka j WHERE j.fkPacient.evidencniCisloPojistence = :ev AND j.datumDo IS NULL", JeZapsanDoLuzka.class)
                    .setParameter("ev", testEvCislo)
                    .getResultList();

            if (!aktivniZapisy.isEmpty() && aktivniZapisy.get(0).getFkLuzko().getFyzickeCislo().equals(bedB.getFyzickeCislo())) {
                System.out.println("Transfer successful! Patient is now in Bed B.");
            } else {
                System.out.println("Transfer verification FAILED.");
            }

        } catch (Exception e) {
            System.err.println("Task 4 test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testStep_4_cleanup(EntityManager em) {
        System.out.println("\n[CLEANUP ACTION - Task 4]");
        services.HospitalService hs = new services.HospitalService(em);
        try {
            hs.removePatientSafely("9998887776");
            System.out.println("Cleanup completed (Patient removed).");
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
        em.clear();
    }

    public static void otherTesting(EntityManager em) {
        Query q = em.createQuery("SELECT o FROM Osoba o");
        List<Osoba> osoba = q.getResultList();

        System.out.println("Ososby (JPQL)");
        for (Osoba o : osoba) {
            System.out.println(o.getJmeno());
        }

        List<Doktor> doktor = em.createQuery("SELECT o FROM Doktor o").getResultList();
        System.out.println("Doktory (JPQL)");
        for (Doktor d : doktor) {
            System.out.println("doc: " + d.getPrijmeni());
            System.out.println("dohledavaci doktor: d");
        }
//
        List<Pacient> pacients = em.createQuery("SELECT o FROM Pacient o").getResultList();
        System.out.println("Pacients (JPQL)");
        for (Pacient p : pacients) {
            System.out.println("pacient: " + p.getPrijmeni() + ", " + p.getKrevniSkupina());
        }


        Doktor doktor2 = em.find(Doktor.class, 101); // change ID as needed

        System.out.println("Doktor: " + doktor2.getJmeno() + " " + doktor2.getPrijmeni());

        System.out.println("\nDoctors this doctor supervises:");
        for (Doktor d : doktor2.getDohledavani()) {
            System.out.println("  - " + d.getJmeno() + " " + d.getPrijmeni());
        }

        System.out.println("\nDoctors who supervise this doctor:");
        for (Doktor d : doktor2.getDohledavaci()) {
            System.out.println("  - " + d.getJmeno() + " " + d.getPrijmeni());
        }

        List<Mistnost> mistnosts = em.createQuery("SELECT o FROM Mistnost o").getResultList();
        System.out.println("Mistnosts (JPQL)");
        for (Mistnost m : mistnosts) {
            System.out.println("mistnost: " + m.getCisloMistnosti());
        }

        List<Luzko> luzkos = em.createQuery("SELECT o FROM Luzko o").getResultList();
        System.out.println("Luzkos (JPQL)");
        for (Luzko l : luzkos) {
            System.out.println("luzko: " + l.getFyzickeCislo() + " " + l.getDulezitostLuzka());
        }

        List<JeZapsanDoLuzka> jeZapsanDoLuzkas = em.createQuery("SELECT o FROM JeZapsanDoLuzka o").getResultList();
        System.out.println("JeZapsan (JPQL)");
        for (JeZapsanDoLuzka j : jeZapsanDoLuzkas) {
            System.out.println("zapsan: " + j.getFkPacient().getPrijmeni() + " on: " + j.getFkLuzko().getFyzickeCislo());
        }

        List<Ukon> ukons = em.createQuery("SELECT o FROM Ukon o").getResultList();
        System.out.println("Ukons (JPQL)");
        for (Ukon u : ukons) {
            System.out.println("ukon: " + u.getNazevUkonu() + ", " + u.getPopisUkonu());
        }

        List<Mistnost> mistnosts2 = em.createQuery("SELECT o FROM Mistnost o").getResultList();
        System.out.println("Mistnosts (JPQL)");
        for (Mistnost m : mistnosts2) {
            System.out.println("mistnost: " + m.getCisloMistnosti() + ", " + m.getBarvaMistnosti() + ", " + m.getOddeleni());
        }

        List<ZdravotniKarta> zdravotniKartas = em.createQuery("SELECT o FROM ZdravotniKarta o").getResultList();
        System.out.println("ZdravotniKartas (JPQL)");
        for (ZdravotniKarta z : zdravotniKartas) {
            System.out.println("zdravotni karta: " + z.getCisloKarty() + ", " + z.getDatumZalozeni() + ", " + z.getStav());
        }

        List<Chorobopis> chorobopises = em.createQuery("SELECT o FROM Chorobopis o").getResultList();
        System.out.println("Chorobopis (JPQL)");
        for (Chorobopis c : chorobopises) {
            System.out.println("chorobopis: " + c.getCisloChorobopisu() + ", " + c.getPopisChorobopisu() + ", " + c.getFkZdravotniKarta().getCisloKarty());
        }

        List<Lek> leks = em.createQuery("SELECT o FROM Lek o").getResultList();
        System.out.println("Leks (JPQL)");
        for (Lek l : leks) {
            System.out.println("lek: " + l.getNazevLeku());
        }

        List<Specializace> specializaces = em.createQuery("SELECT o FROM Specializace o").getResultList();
        System.out.println("Specializaces (JPQL)");
        for (Specializace s : specializaces) {
            System.out.println("specializace: " + s.getSpecializace() + " for doc: " + s.getFkOsoba().getEvidencniCisloClk());
        }

        // Testing new DAOs
        System.out.println("\n--- Testing new DAOs ---");
        DAO.JeZapsanDoLuzkaDAO jeZapsanDAO = new DAO.JeZapsanDoLuzkaDAO(em);
        DAO.ProvedeniUkonuDAO provedeniDAO = new DAO.ProvedeniUkonuDAO(em);

        if (!pacients.isEmpty()) {
            Pacient firstPacient = pacients.get(0);
            System.out.println("Testing with Pacient: " + firstPacient.getPrijmeni());

            List<JeZapsanDoLuzka> pacientsLuzka = jeZapsanDAO.selectByPacient(firstPacient);
            System.out.println("Pacient's Bed Registrations: " + pacientsLuzka.size());

            List<ProvedeniUkonu> pacientsUkony = provedeniDAO.selectByPacient(firstPacient);
            System.out.println("Pacient's Procedures: " + pacientsUkony.size());
        } else {
            System.out.println("No pacients found to test with.");
        }

        // DAO:
        OsobaDAO osobaDAO = new OsobaDAO(em);

        // --- CREATE (Insert) ---
        Osoba novaOsoba = new Osoba();
        novaOsoba.setEvidencniCisloPojistence("1234567890");
        novaOsoba.setJmeno("Jan");
        novaOsoba.setPrijmeni("Novák");
        novaOsoba.setDatumNarozeni(LocalDate.of(1985, 5, 20));
        novaOsoba.setMesto("Praha");

        osobaDAO.insert(novaOsoba);
        System.out.println("Vložena osoba s ID: " + novaOsoba.getId());
        // --- READ (Select) ---
        // Get by ID
        Osoba nalezenaOsoba = osobaDAO.selectById(novaOsoba.getId());
        System.out.println("Nalezena osoba: " + nalezenaOsoba.getJmeno() + " " + nalezenaOsoba.getPrijmeni());
        // Get by custom method we added to OsobaDAO
        Osoba podleCisla = osobaDAO.selectByEvidencniCislo("1234567890");
        System.out.println("Nalezena podle čísla: " + podleCisla.getMesto());
        // --- UPDATE ---
        nalezenaOsoba.setMesto("Brno");
        osobaDAO.update(nalezenaOsoba);
        // --- DELETE ---
        osobaDAO.deleteById(nalezenaOsoba.getId());
    }
}

