-- =============================================================================
-- SQL DOTAZY – Nemocniční databáze
-- Pokrývá: vnější spojení, vnitřní spojení, podmínku na data, agregaci
--          s HAVING, řazení a stránkování, množinové operace, vnořený SELECT
-- =============================================================================
-- -----------------------------------------------------------------------------
-- 1. VNITŘNÍ SPOJENÍ (INNER JOIN)
--    Seznam všech provedených úkonů s jménem pacienta, doktora a názvem úkonu
-- -----------------------------------------------------------------------------
SELECT
    pu.provedeni_ukonu_id,
    pu.datum,
    pu.cas,
    pac_o.jmeno AS pacient_jmeno,
    pac_o.prijmeni AS pacient_prijmeni,
    dok_o.jmeno AS doktor_jmeno,
    dok_o.prijmeni AS doktor_prijmeni,
    u.nazev_ukonu
FROM
    Provedeni_ukonu pu
    INNER JOIN Pacient pac ON pac.osoba_id = pu.fk_pacient_id
    INNER JOIN Osoba pac_o ON pac_o.osoba_id = pac.osoba_id
    INNER JOIN Doktor dok ON dok.osoba_id = pu.fk_doktor_id
    INNER JOIN Osoba dok_o ON dok_o.osoba_id = dok.osoba_id
    INNER JOIN Ukon u ON u.ukon_id = pu.fk_ukon_id
ORDER BY
    pu.datum DESC,
    pu.cas DESC;

-- -----------------------------------------------------------------------------
-- 2. VNĚJŠÍ SPOJENÍ (LEFT OUTER JOIN)
--    Všichni pacienti včetně těch, kteří ještě nebyli zapsáni na žádné lůžko
-- -----------------------------------------------------------------------------
SELECT
    o.osoba_id,
    o.jmeno,
    o.prijmeni,
    p.krevni_skupina,
    l.fyzicke_cislo AS cislo_luzka,
    m.cislo_mistnosti,
    m.oddeleni,
    jzl.datum_od,
    jzl.datum_do
FROM
    Pacient p
    INNER JOIN Osoba o ON o.osoba_id = p.osoba_id
    LEFT JOIN Je_zapsan_do_luzka jzl ON jzl.fk_pacient_id = p.osoba_id
    LEFT JOIN Luzko l ON l.luzko_id = jzl.fk_luzko_id
    LEFT JOIN Mistnost m ON m.mistnost_id = l.fk_mistnost_id
ORDER BY
    o.prijmeni,
    o.jmeno;

-- -----------------------------------------------------------------------------
-- 3. PODMÍNKA NA DATA
--    Úkony provedené v posledních 6 měsících na oddělení kardiologie nebo
--    neurologie, pouze pro pacienty s krevní skupinou A+ nebo 0+
-- -----------------------------------------------------------------------------
SELECT
    pu.datum,
    pu.cas,
    pac_o.jmeno || ' ' || pac_o.prijmeni AS pacient,
    dok_o.jmeno || ' ' || dok_o.prijmeni AS doktor,
    u.nazev_ukonu,
    pac.krevni_skupina
FROM
    Provedeni_ukonu pu
    INNER JOIN Pacient pac ON pac.osoba_id = pu.fk_pacient_id
    INNER JOIN Osoba pac_o ON pac_o.osoba_id = pac.osoba_id
    INNER JOIN Doktor dok ON dok.osoba_id = pu.fk_doktor_id
    INNER JOIN Osoba dok_o ON dok_o.osoba_id = dok.osoba_id
    INNER JOIN Ukon u ON u.ukon_id = pu.fk_ukon_id
    INNER JOIN Specializace s ON s.fk_osoba_id = dok.osoba_id
WHERE
    pu.datum >= CURRENT_DATE - INTERVAL '6 months'
    AND pac.krevni_skupina IN ('A+', '0+')
    AND s.specializace IN ('Kardiologie', 'Neurologie')
ORDER BY
    pu.datum DESC;

-- -----------------------------------------------------------------------------
-- 4. AGREGACE + HAVING
--    Doktoři, kteří provedli více než 50 úkonů celkem,
--    seřazení podle počtu úkonů sestupně
-- -----------------------------------------------------------------------------
SELECT
    o.jmeno,
    o.prijmeni,
    d.icl,
    COUNT(pu.provedeni_ukonu_id) AS pocet_ukonu,
    MIN(pu.datum) AS prvni_ukon,
    MAX(pu.datum) AS posledni_ukon
FROM
    Doktor d
    INNER JOIN Osoba o ON o.osoba_id = d.osoba_id
    INNER JOIN Provedeni_ukonu pu ON pu.fk_doktor_id = d.osoba_id
GROUP BY
    d.osoba_id,
    o.jmeno,
    o.prijmeni,
    d.icl
HAVING
    COUNT(pu.provedeni_ukonu_id) > 50
ORDER BY
    pocet_ukonu DESC;

-- -----------------------------------------------------------------------------
-- 5. ŘAZENÍ A STRÁNKOVÁNÍ
--    Abecední seznam pacientů s počtem jejich úkonů – stránka 3 (po 20 záznamech)
-- -----------------------------------------------------------------------------
SELECT
    o.prijmeni,
    o.jmeno,
    o.datum_narozeni,
    p.krevni_skupina,
    COUNT(pu.provedeni_ukonu_id) AS pocet_ukonu
FROM
    Pacient p
    INNER JOIN Osoba o ON o.osoba_id = p.osoba_id
    LEFT JOIN Provedeni_ukonu pu ON pu.fk_pacient_id = p.osoba_id
GROUP BY
    p.osoba_id,
    o.prijmeni,
    o.jmeno,
    o.datum_narozeni,
    p.krevni_skupina
ORDER BY
    o.prijmeni ASC,
    o.jmeno ASC
LIMIT
    20
OFFSET
    40;

-- stránka 3 (0-based: 0=str.1, 20=str.2, 40=str.3)
-- -----------------------------------------------------------------------------
-- 6. MNOŽINOVÉ OPERACE (UNION / EXCEPT / INTERSECT)
--
--    6a. UNION – osoby vystupující jako doktor NEBO jako pacient (nebo obojí)
-- -----------------------------------------------------------------------------
SELECT
    o.osoba_id,
    o.jmeno,
    o.prijmeni,
    'doktor' AS role
FROM
    Doktor d
    INNER JOIN Osoba o ON o.osoba_id = d.osoba_id
UNION
SELECT
    o.osoba_id,
    o.jmeno,
    o.prijmeni,
    'pacient' AS role
FROM
    Pacient p
    INNER JOIN Osoba o ON o.osoba_id = p.osoba_id
ORDER BY
    prijmeni,
    jmeno;

--    6b. INTERSECT – úkony, které jsou zároveň přiřazeny nějakému doktorovi
--        (Kvalifikace_doktora) A zároveň mají registrovaný alespoň jeden lék
SELECT
    ukon_id
FROM
    Kvalifikace_doktora
INTERSECT
SELECT
    ukon_id
FROM
    Registrovane_leky_pro_ukon
ORDER BY
    ukon_id;

--    6c. EXCEPT – úkony, které NEJSOU kvalifikovány žádným doktorem
SELECT
    ukon_id
FROM
    Ukon
EXCEPT
SELECT
    ukon_id
FROM
    Kvalifikace_doktora
ORDER BY
    ukon_id;

-- -----------------------------------------------------------------------------
-- 7. VNOŘENÝ SELECT (subquery)
--
--    7a. Pacienti, kteří podstoupili VÍCE úkonů než je průměr přes všechny pacienty
-- -----------------------------------------------------------------------------
SELECT
    o.jmeno,
    o.prijmeni,
    p.krevni_skupina,
    COUNT(pu.provedeni_ukonu_id) AS pocet_ukonu
FROM
    Pacient p
    INNER JOIN Osoba o ON o.osoba_id = p.osoba_id
    INNER JOIN Provedeni_ukonu pu ON pu.fk_pacient_id = p.osoba_id
GROUP BY
    p.osoba_id,
    o.jmeno,
    o.prijmeni,
    p.krevni_skupina
HAVING
    COUNT(pu.provedeni_ukonu_id) > (
        -- průměrný počet úkonů na pacienta
        SELECT
            AVG(cnt)
        FROM
            (
                SELECT
                    COUNT(*) AS cnt
                FROM
                    Provedeni_ukonu
                GROUP BY
                    fk_pacient_id
            ) sub
    )
ORDER BY
    pocet_ukonu DESC;

--    7b. Doktoři, kteří dosud NEDOHLEDÁVAJÍ nikoho (nejsou v roli dohledávajícího)
SELECT
    o.jmeno,
    o.prijmeni,
    d.icl
FROM
    Doktor d
    INNER JOIN Osoba o ON o.osoba_id = d.osoba_id
WHERE
    d.osoba_id NOT IN (
        SELECT
            dohledavaci_osoba_id
        FROM
            Dohledani
    )
ORDER BY
    o.prijmeni,
    o.jmeno;

--    7c. Nejčastěji prováděný úkon (může jich být více při shodě)
SELECT
    u.ukon_id,
    u.nazev_ukonu,
    pocty.pocet
FROM
    Ukon u
    INNER JOIN (
        SELECT
            fk_ukon_id,
            COUNT(*) AS pocet
        FROM
            Provedeni_ukonu
        GROUP BY
            fk_ukon_id
    ) pocty ON pocty.fk_ukon_id = u.ukon_id
WHERE
    pocty.pocet = (
        SELECT
            MAX(cnt)
        FROM
            (
                SELECT
                    COUNT(*) AS cnt
                FROM
                    Provedeni_ukonu
                GROUP BY
                    fk_ukon_id
            ) sub
    );