-- =============================================================================
-- Give permissions, GRANTS
-- =============================================================================
GRANT CONNECT ON DATABASE kalasan1 TO syrovji1;

GRANT USAGE ON SCHEMA public TO syrovji1;

GRANT
SELECT
,
    INSERT,
UPDATE,
DELETE ON ALL TABLES IN SCHEMA public TO syrovji1;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT
SELECT
,
    INSERT,
UPDATE,
DELETE ON TABLES TO syrovji1;

-- =============================================================================
-- Drop existing objects
-- =============================================================================
DROP TABLE IF EXISTS Registrovane_leky_pro_ukon CASCADE;

DROP TABLE IF EXISTS Vlastni CASCADE;

DROP TABLE IF EXISTS Je_zapsan_do_luzka CASCADE;

DROP TABLE IF EXISTS Provedeni_ukonu CASCADE;

DROP TABLE IF EXISTS Kvalifikace_doktora CASCADE;

DROP TABLE IF EXISTS Dohledani CASCADE;

DROP TABLE IF EXISTS Specializace CASCADE;

DROP TABLE IF EXISTS Pacient CASCADE;

DROP TABLE IF EXISTS Doktor CASCADE;

DROP TABLE IF EXISTS Lek CASCADE;

DROP TABLE IF EXISTS Luzko CASCADE;

DROP TABLE IF EXISTS Chorobopis CASCADE;

DROP TABLE IF EXISTS Zdravotni_karta CASCADE;

DROP TABLE IF EXISTS Ukon CASCADE;

DROP TABLE IF EXISTS Mistnost CASCADE;

DROP TABLE IF EXISTS Osoba CASCADE;

DROP TYPE IF EXISTS dulezitost_luzka_enum CASCADE;

DROP TYPE IF EXISTS stav_enum CASCADE;

DROP TYPE IF EXISTS oddeleni_enum CASCADE;

DROP TYPE IF EXISTS barva_mistnosti_enum CASCADE;

-- =============================================================================
-- ENUMS
-- =============================================================================
CREATE TYPE barva_mistnosti_enum AS ENUM ('fialova', 'zelena', 'modra', 'bila');

CREATE TYPE oddeleni_enum AS ENUM (
    'chirurgie',
    'interna',
    'neurologie',
    'kardiologie',
    'pediatrie',
    'psychiatrie',
    'ortopedie',
    'onkologie'
);

CREATE TYPE stav_enum AS ENUM ('aktivní', 'neaktivní', 'archivovaný');

CREATE TYPE dulezitost_luzka_enum AS ENUM ('běžná', 'intenzivní', 'jednotka_intenzivní_péče');

-- =============================================================================
-- CREATE TABLES
-- =============================================================================
CREATE TABLE
    Osoba (
        osoba_id SERIAL NOT NULL,
        evidencni_cislo_pojistence char(10) NOT NULL,
        jmeno varchar(100) NOT NULL,
        prijmeni varchar(100) NOT NULL,
        datum_narozeni date NOT NULL,
        mesto varchar(100),
        ulice varchar(100),
        stat varchar(100),
        cislo_nemocnice varchar(20),
        PRIMARY KEY (osoba_id),
        UNIQUE (evidencni_cislo_pojistence),
        CHECK (datum_narozeni <= CURRENT_DATE),
        CHECK (datum_narozeni >= '1900-01-01')
    );

CREATE TABLE
    Mistnost (
        mistnost_id SERIAL NOT NULL,
        cislo_mistnosti varchar(20) NOT NULL,
        barva_mistnosti barva_mistnosti_enum NOT NULL,
        oddeleni oddeleni_enum NOT NULL,
        PRIMARY KEY (mistnost_id),
        UNIQUE (cislo_mistnosti, barva_mistnosti, oddeleni)
    );

CREATE TABLE
    Ukon (
        ukon_id SERIAL NOT NULL,
        nazev_ukonu varchar(100) NOT NULL,
        popis_ukonu text NOT NULL,
        PRIMARY KEY (ukon_id),
        UNIQUE (nazev_ukonu)
    );

CREATE TABLE
    Zdravotni_karta (
        zdravotni_karta_id SERIAL NOT NULL,
        cislo_karty varchar(20) NOT NULL,
        stav stav_enum NOT NULL DEFAULT 'aktivní',
        datum_zalozeni date NOT NULL DEFAULT CURRENT_DATE,
        PRIMARY KEY (zdravotni_karta_id),
        UNIQUE (cislo_karty),
        CHECK (datum_zalozeni <= CURRENT_DATE),
        CHECK (datum_zalozeni >= '1900-01-01')
    );

CREATE TABLE
    Chorobopis (
        chorobopis_id SERIAL NOT NULL,
        cislo_chorobopisu varchar(20) NOT NULL,
        datum_od date NOT NULL,
        datum_do date,
        popis_chorobopisu text NOT NULL,
        fk_zdravotni_karta_id int4 NOT NULL,
        PRIMARY KEY (chorobopis_id),
        UNIQUE (cislo_chorobopisu),
        CHECK (
            datum_do IS NULL
            OR datum_do > datum_od
        ),
        CHECK (datum_od <= CURRENT_DATE),
        CONSTRAINT Ref_Chorobopis_to_Zdravotni_karta FOREIGN KEY (fk_zdravotni_karta_id) REFERENCES Zdravotni_karta (zdravotni_karta_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Luzko (
        luzko_id SERIAL NOT NULL,
        fyzicke_cislo varchar(20) NOT NULL,
        fk_mistnost_id int4 NOT NULL,
        dulezitost_luzka dulezitost_luzka_enum NOT NULL DEFAULT 'běžná',
        PRIMARY KEY (luzko_id),
        UNIQUE (fyzicke_cislo, fk_mistnost_id),
        CONSTRAINT Ref_Luzko_to_Mistnost FOREIGN KEY (fk_mistnost_id) REFERENCES Mistnost (mistnost_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Lek (
        lek_id SERIAL NOT NULL,
        nazev_leku varchar(100) NOT NULL,
        PRIMARY KEY (lek_id),
        UNIQUE (nazev_leku)
    );

CREATE TABLE
    Doktor (
        osoba_id SERIAL NOT NULL,
        icl char(6) NOT NULL,
        evidencni_cislo_clk char(6) NOT NULL,
        identifikator_nrzp char(9) NOT NULL,
        CONSTRAINT dok_id PRIMARY KEY (osoba_id),
        CONSTRAINT UQ_icl UNIQUE (icl),
        CONSTRAINT UQ_clk UNIQUE (evidencni_cislo_clk),
        CONSTRAINT UQ_nrzp UNIQUE (identifikator_nrzp),
        CONSTRAINT Ref_Doktor_to_Osoba FOREIGN KEY (osoba_id) REFERENCES Osoba (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Pacient (
        osoba_id SERIAL NOT NULL,
        krevni_skupina varchar(3),
        PRIMARY KEY (osoba_id),
        CONSTRAINT Ref_Pacient_to_Osoba FOREIGN KEY (osoba_id) REFERENCES Osoba (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Specializace (
        specializace_id SERIAL NOT NULL,
        fk_osoba_id int4 NOT NULL,
        specializace varchar(100) NOT NULL,
        PRIMARY KEY (specializace_id),
        UNIQUE (fk_osoba_id, specializace),
        CONSTRAINT Ref_Specializace_to_Doktor FOREIGN KEY (fk_osoba_id) REFERENCES Doktor (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Dohledani (
        dohledavaci_osoba_id int4 NOT NULL,
        dohledavany_osoba_id int4 NOT NULL,
        UNIQUE (dohledavaci_osoba_id, dohledavany_osoba_id),
        CHECK (dohledavaci_osoba_id <> dohledavany_osoba_id),
        PRIMARY KEY (dohledavaci_osoba_id, dohledavany_osoba_id),
        CONSTRAINT Ref_Dohledani_to_Doktor_dohledavaci FOREIGN KEY (dohledavaci_osoba_id) REFERENCES Doktor (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Dohledani_to_Doktor_dohledavany FOREIGN KEY (dohledavany_osoba_id) REFERENCES Doktor (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Kvalifikace_doktora (
        doktor_id int4 NOT NULL,
        ukon_id int4 NOT NULL,
        PRIMARY KEY (doktor_id, ukon_id),
        UNIQUE (doktor_id, ukon_id),
        CONSTRAINT Ref_Kvalifikace_doktora_to_Doktor FOREIGN KEY (doktor_id) REFERENCES Doktor (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Kvalifikace_doktora_to_Ukon FOREIGN KEY (ukon_id) REFERENCES Ukon (ukon_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Provedeni_ukonu (
        provedeni_ukonu_id SERIAL NOT NULL,
        fk_pacient_id int4 NOT NULL,
        fk_doktor_id int4 NOT NULL,
        fk_ukon_id int4 NOT NULL,
        datum date NOT NULL,
        cas time NOT NULL,
        PRIMARY KEY (provedeni_ukonu_id),
        UNIQUE (
            fk_pacient_id,
            fk_doktor_id,
            fk_ukon_id,
            datum,
            cas
        ),
        CHECK (datum <= CURRENT_DATE),
        CONSTRAINT Ref_Provedeni_ukonu_to_Pacient FOREIGN KEY (fk_pacient_id) REFERENCES Pacient (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Provedeni_ukonu_to_Doktor FOREIGN KEY (fk_doktor_id) REFERENCES Doktor (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Provedeni_ukonu_to_Ukon FOREIGN KEY (fk_ukon_id) REFERENCES Ukon (ukon_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Je_zapsan_do_luzka (
        je_zapsan_do_luzka_id SERIAL NOT NULL,
        fk_pacient_id int4 NOT NULL,
        fk_luzko_id int4 NOT NULL,
        datum_od timestamp NOT NULL,
        datum_do timestamp,
        PRIMARY KEY (je_zapsan_do_luzka_id),
        UNIQUE (fk_pacient_id, fk_luzko_id, datum_od),
        CHECK (
            datum_do IS NULL
            OR datum_do > datum_od
        ),
        CHECK (datum_od <= CURRENT_TIMESTAMP),
        CONSTRAINT Ref_Je_zapsan_do_luzka_to_Pacient FOREIGN KEY (fk_pacient_id) REFERENCES Pacient (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Je_zapsan_do_luzka_to_Luzko FOREIGN KEY (fk_luzko_id) REFERENCES Luzko (luzko_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Vlastni (
        zdravotni_karta_id int4 NOT NULL,
        osoba_id int4 NOT NULL,
        PRIMARY KEY (zdravotni_karta_id, osoba_id),
        CONSTRAINT UQ_zdravKarta_pacientId UNIQUE (zdravotni_karta_id, osoba_id),
        CONSTRAINT Ref_Vlastni_to_Zdravotni_karta FOREIGN KEY (zdravotni_karta_id) REFERENCES Zdravotni_karta (zdravotni_karta_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Vlastni_to_Pacient FOREIGN KEY (osoba_id) REFERENCES Pacient (osoba_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE TABLE
    Registrovane_leky_pro_ukon (
        lek_id int4 NOT NULL,
        ukon_id int4 NOT NULL,
        PRIMARY KEY (lek_id, ukon_id),
        CONSTRAINT Ref_Registrovane_leky_pro_ukon_to_Ukon FOREIGN KEY (ukon_id) REFERENCES Ukon (ukon_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT Ref_Registrovane_leky_pro_ukon_to_Lek FOREIGN KEY (lek_id) REFERENCES Lek (lek_id) MATCH SIMPLE ON DELETE CASCADE ON UPDATE CASCADE
    );
