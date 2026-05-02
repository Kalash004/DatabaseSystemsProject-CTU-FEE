-- =============================================================================
-- SQL POKROCILE DOTAZY – Nemocniční databáze
-- =============================================================================
-- -----------------------------------------------------------------------------
-- 1. TRANSACTION
-- -----------------------------------------------------------------------------
BEGIN;
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 1. Aktualizace stavu zdravotní karty na 'aktivní'
UPDATE Zdravotni_karta 
SET stav = 'aktivní' 
WHERE zdravotni_karta_id = (SELECT zdravotni_karta_id FROM Vlastni WHERE osoba_id = 10);

-- ROLLBACK;

-- 2. Zápis pacienta na lůžko
INSERT INTO Je_zapsan_do_luzka (fk_pacient_id, fk_luzko_id, datum_od)
VALUES (10, 5, CURRENT_TIMESTAMP);

COMMIT;

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
-- 4. INDEX
-- -----------------------------------------------------------------------------
CREATE INDEX idx_osoba_prijmeni_jmeno ON Osoba (prijmeni, jmeno);

-- Analýza využití indexu v dotazu:
EXPLAIN ANALYZE
SELECT * FROM Osoba WHERE prijmeni = 'Novák' AND jmeno = 'Jan';