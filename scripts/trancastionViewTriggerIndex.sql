-- =============================================================================
-- SQL POKROCILE DOTAZY – Nemocniční databáze
-- =============================================================================
-- -----------------------------------------------------------------------------
-- 1. TRANSACTION
-- -----------------------------------------------------------------------------
SELECT * FROM zdravotni_karta
WHERE zdravotni_karta_id = (SELECT zdravotni_karta_id FROM Vlastni WHERE osoba_id = 501);

BEGIN;
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 1. Aktualizace stavu zdravotní karty na 'aktivní'
UPDATE Zdravotni_karta
SET stav = 'neaktivní'
WHERE zdravotni_karta_id = (SELECT zdravotni_karta_id FROM Vlastni WHERE osoba_id = 501);

-- 2. Zápis pacienta na lůžko
INSERT INTO Je_zapsan_do_luzka (fk_pacient_id, fk_luzko_id, datum_od)
VALUES (10, 5, CURRENT_TIMESTAMP);

ROLLBACK;

COMMIT;

SELECT * FROM zdravotni_karta
WHERE zdravotni_karta_id = (SELECT zdravotni_karta_id FROM Vlastni WHERE osoba_id = 501);

-- -----------------------------------------------------------------------------
-- 2. VIEW
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_obsazena_luzka AS
SELECT
    o.jmeno,
    o.prijmeni,
    m.oddeleni,
    l.fyzicke_cislo AS cislo_luzka,
    jzl.datum_od
FROM Je_zapsan_do_luzka jzl
         JOIN Pacient p ON jzl.fk_pacient_id = p.osoba_id
         JOIN Osoba o ON o.osoba_id = p.osoba_id
         JOIN Luzko l ON l.luzko_id = jzl.fk_luzko_id
         JOIN Mistnost m ON m.mistnost_id = l.fk_mistnost_id
WHERE jzl.datum_do IS NULL;

-- Použití:
SELECT * FROM v_obsazena_luzka WHERE oddeleni = 'kardiologie';

-- -----------------------------------------------------------------------------
-- 3. TRIGGER
-- -----------------------------------------------------------------------------
-- 1. Vytvoření obslužné funkce s logikou kontroly
CREATE OR REPLACE FUNCTION fnc_kontrola_kvalifikace()
    RETURNS TRIGGER
AS $$
BEGIN
    -- Kontrola, zda existuje vazba mezi doktorem a úkonem v číselníku kvalifikací
    -- existuje alespoň jeden záznam, který odpovídá podmínce (SELECT 1)
    IF NOT EXISTS (
        SELECT 1
        FROM Kvalifikace_doktora
        WHERE doktor_id = NEW.fk_doktor_id
          AND ukon_id = NEW.fk_ukon_id
    ) THEN
        -- Pokud kvalifikace chybí, vyvoláme výjimku a zápis se neprovede
        RAISE EXCEPTION 'NEPOVOLENÁ OPERACE: Doktor (ID %) nemá potřebnou kvalifikaci pro úkon (ID %)!',
            NEW.fk_doktor_id, NEW.fk_ukon_id;
    END IF;

    -- Pokud je vše v pořádku, dovolíme zápis
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Definice triggeru na tabulce Provedeni_ukonu
CREATE TRIGGER trg_check_doktor_kvalifikace
    BEFORE INSERT ON Provedeni_ukonu
    FOR EACH ROW
EXECUTE FUNCTION fnc_kontrola_kvalifikace();

-- Tento INSERT narazí na trigger a skončí chybou
INSERT INTO Provedeni_ukonu (fk_pacient_id, fk_doktor_id, fk_ukon_id, datum, cas)
VALUES (1, 5, 10, CURRENT_DATE, '14:30:00');

-- Výsledek: ERROR: NEPOVOLENÁ OPERACE: Doktor (ID 5) nemá potřebnou kvalifikaci pro úkon (ID 10)!

-- -----------------------------------------------------------------------------
-- 3. TRIGGER
--		- kontrola veku
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fnc_kontrola_veku_pediatrie()
    RETURNS TRIGGER AS $$
DECLARE
    v_vek INT;
    v_oddeleni oddeleni_enum;
BEGIN
    -- Získání věku pacienta
    SELECT EXTRACT(YEAR FROM AGE(o.datum_narozeni)) INTO v_vek
    FROM Osoba o WHERE o.osoba_id = NEW.fk_pacient_id;

    -- Získání oddělení, kam patří lůžko
    SELECT m.oddeleni INTO v_oddeleni
    FROM Luzko l JOIN Mistnost m ON l.fk_mistnost_id = m.mistnost_id
    WHERE l.luzko_id = NEW.fk_luzko_id;

    IF v_oddeleni = 'pediatrie' AND v_vek >= 18 THEN
        RAISE EXCEPTION 'Na pediatrii nelze zapsat dospělého pacienta (věk: %)!', v_vek;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_kontrola_veku
    BEFORE INSERT ON Je_zapsan_do_luzka
    FOR EACH ROW
EXECUTE FUNCTION fnc_kontrola_veku_pediatrie();

-- 1. Vložíme dospělou osobu (např. 30 let)
INSERT INTO Osoba (evidencni_cislo_pojistence, jmeno, prijmeni, datum_narozeni, mesto)
VALUES ('9001011234', 'Jan', 'Dospělý', '1994-01-01', 'Praha');

-- 2. Vložíme ji do tabulky Pacient
INSERT INTO Pacient (osoba_id, krevni_skupina)
VALUES ((SELECT osoba_id FROM Osoba WHERE prijmeni = 'Dospělý'), 'A+');

-- 3. Vytvoříme místnost na pediatrii
INSERT INTO Mistnost (cislo_mistnosti, barva_mistnosti, oddeleni)
VALUES ('P100', 'modra', 'pediatrie');

-- 4. Přidáme do této místnosti lůžko
INSERT INTO Luzko (fyzicke_cislo, fk_mistnost_id, dulezitost_luzka)
VALUES ('L-01', (SELECT mistnost_id FROM Mistnost WHERE cislo_mistnosti = 'P100'), 'běžná');

-- Tento INSERT narazí na trigger a skončí chybou
INSERT INTO Je_zapsan_do_luzka (fk_pacient_id, fk_luzko_id, datum_od)
VALUES (
           (SELECT osoba_id FROM Osoba WHERE prijmeni = 'Dospělý'),
           (SELECT luzko_id FROM Luzko WHERE fyzicke_cislo = 'L-01'),
           CURRENT_TIMESTAMP
       );

-- Výsledek: ERROR: Na pediatrii nelze zapsat dospělého pacienta (věk: 32)!
-- -----------------------------------------------------------------------------
-- 4. INDEX
-- -----------------------------------------------------------------------------
DROP INDEX if exists idx_osoba_prijmeni_jmeno;

EXPLAIN ANALYZE
SELECT * FROM Osoba WHERE prijmeni = 'Novák' AND jmeno = 'Jan';

CREATE INDEX idx_osoba_prijmeni_jmeno ON Osoba (prijmeni, jmeno);

-- Analýza využití indexu v dotazu:
EXPLAIN ANALYZE
SELECT * FROM Osoba WHERE prijmeni = 'Novák' AND jmeno = 'Jan';