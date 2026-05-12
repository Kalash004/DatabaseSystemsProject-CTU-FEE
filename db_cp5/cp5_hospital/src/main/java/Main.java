import entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        EntityManager em = emf.createEntityManager();

        Query q = em.createQuery("SELECT o FROM Osoba o");
        List<Osoba> osoba = q.getResultList();

        System.out.println("Ososby (JPQL)");
        for (Osoba o : osoba) {
            System.out.println(o.getJmeno());
        }

        List<Doktor> doktor = em.createQuery("SELECT o FROM Doktor o").getResultList();
        System.out.println("Doktory (JPQL)");
        for (Doktor d : doktor) {
            System.out.println("doc: " + d.getOsoba().getPrijmeni());
            System.out.println("dohledavaci doktor: d");
        }
//
        List<Pacient> pacients = em.createQuery("SELECT o FROM Pacient o").getResultList();
        System.out.println("Pacients (JPQL)");
        for (Pacient p : pacients) {
            System.out.println("pacient: " + p.getOsoba().getPrijmeni() + ", " + p.getKrevniSkupina());
        }


        Doktor doktor2 = em.find(Doktor.class, 101); // change ID as needed

        System.out.println("Doktor: " + doktor2.getOsoba().getJmeno() + " " + doktor2.getOsoba().getPrijmeni());

        System.out.println("\nDoctors this doctor supervises:");
        for (Doktor d : doktor2.getDohledavani()) {
            System.out.println("  - " + d.getOsoba().getJmeno() + " " + d.getOsoba().getPrijmeni());
        }

        System.out.println("\nDoctors who supervise this doctor:");
        for (Doktor d : doktor2.getDohledavaci()) {
            System.out.println("  - " + d.getOsoba().getJmeno() + " " + d.getOsoba().getPrijmeni());
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

        List<JeZapsanDoLuzka> jeZapsanDoLuzkas  = em.createQuery("SELECT o FROM JeZapsanDoLuzka o").getResultList();
        System.out.println("JeZapsan (JPQL)");
        for (JeZapsanDoLuzka j : jeZapsanDoLuzkas) {
            System.out.println("zapsan: "+ j.getFkPacient().getOsoba().getPrijmeni() + " on: " + j.getFkLuzko().getFyzickeCislo());
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
            System.out.println("specializace: " + s.getSpecializace() + " for doc: "+ s.getFkOsoba().getEvidencniCisloClk());
        }

    }
}
