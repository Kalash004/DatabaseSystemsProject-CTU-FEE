TRUNCATE osoba CASCADE;
truncate chorobopis cascade;
truncate ukon cascade;
-- data populated in: osoba, doctor, pacient, mistnost, luzko, dohledani, ukon

INSERT INTO osoba (evidencni_cislo_pojistence,
                   jmeno,
                   prijmeni,
                   datum_narozeni,
                   mesto,
                   ulice,
                   stat,
                   cislo_nemocnice)
VALUES ('1234567890', 'Jan', 'Novák', '1985-05-12', 'Praha', 'Vodičkova 10', 'Czech Republic', 'HOSP001'),
       ('2345678901', 'Marie', 'Svobodová', '1992-08-23', 'Brno', 'Česká 5', 'Czech Republic', 'HOSP002'),
       ('3456789012', 'Petr', 'Černý', '1978-11-03', 'Ostrava', 'Nádražní 42', 'Czech Republic', 'HOSP001'),
       ('4567890123', 'Lucie', 'Kučerová', '2000-01-15', 'Plzeň', 'Americká 12', 'Czech Republic', 'HOSP003'),
       ('5678901234', 'Tomáš', 'Dvořák', '1965-03-30', 'Liberec', 'Pražská 3', 'Czech Republic', 'HOSP002'),
       ('6789012345', 'Petra', 'Němcová', '1995-07-14', 'Pardubice', 'Hlavní 123', 'Czech Republic', 'HOSP003'),
       ('7890123456', 'Jiří', 'Král', '1982-12-05', 'Hradec Králové', 'Riegrovo nám. 8', 'Czech Republic', 'HOSP001'),
       ('8901234567', 'Kateřina', 'Malá', '1990-04-22', 'Zlín', 'Školní 500', 'Czech Republic', 'HOSP004'),
       ('9012345678', 'Lukáš', 'Urban', '1973-09-09', 'České Budějovice', 'Lidická 15', 'Czech Republic', 'HOSP003'),
       ('0123456789', 'Anna', 'Vlčková', '1988-02-28', 'Olomouc', 'Pekařská 2', 'Czech Republic', 'HOSP002');

select *
from osoba;

INSERT INTO pacient (osoba_id, krevni_skupina)
VALUES ((SELECT osoba_id FROM osoba WHERE jmeno = 'Jan' AND prijmeni = 'Novák'), 'A+'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Marie' AND prijmeni = 'Svobodová'), '0-'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Petr' AND prijmeni = 'Černý'), 'B+'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Petra' AND prijmeni = 'Němcová'), 'AB+'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Jiří' AND prijmeni = 'Král'), '0+'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Lukáš' AND prijmeni = 'Urban'), 'A-'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Anna' AND prijmeni = 'Vlčková'), 'B+');


select *
from pacient
         join osoba on pacient.osoba_id = osoba.osoba_id;


INSERT INTO doktor (osoba_id, icl, evidencni_cislo_clk, identifikator_nrzp)
VALUES ((SELECT osoba_id FROM osoba WHERE jmeno = 'Lucie' AND prijmeni = 'Kučerová'),
        '100001', 'CLK001', 'NRZP00001'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Kateřina' AND prijmeni = 'Malá'),
        '100002', 'CLK002', 'NRZP00002'),
       ((SELECT osoba_id FROM osoba WHERE jmeno = 'Tomáš' AND prijmeni = 'Dvořák'),
        '100003', 'CLK003', 'NRZP00003');

select *
from doktor
         join osoba on doktor.osoba_id = osoba.osoba_id;

INSERT INTO mistnost (cislo_mistnosti,
                      barva_mistnosti,
                      oddeleni)
VALUES ('101', 'bila', 'chirurgie'),
       ('102', 'modra', 'interna'),
       ('201', 'zelena', 'pediatrie'),
       ('202', 'fialova', 'kardiologie'),
       ('305', 'bila', 'neurologie');

select *
from mistnost;

INSERT INTO luzko (fyzicke_cislo,
                   fk_mistnost_id,
                   dulezitost_luzka)
VALUES
    -- Standardní lůžka na chirurgii (místnost 101)
    ('L-101-1', (SELECT mistnost_id FROM mistnost WHERE cislo_mistnosti = '101'), 'běžná'),
    ('L-101-2', (SELECT mistnost_id FROM mistnost WHERE cislo_mistnosti = '101'), 'běžná'),

    -- Intenzivní péče na interně (místnost 102)
    ('L-102-INT', (SELECT mistnost_id FROM mistnost WHERE cislo_mistnosti = '102'), 'intenzivní'),

    -- JIP na kardiologii (místnost 202)
    ('L-202-JIP', (SELECT mistnost_id FROM mistnost WHERE cislo_mistnosti = '202'), 'jednotka_intenzivní_péče'),

    -- Standardní lůžko na pediatrii (místnost 201)
    ('L-201-1', (SELECT mistnost_id FROM mistnost WHERE cislo_mistnosti = '201'), 'běžná');

select *
from luzko;

INSERT INTO dohledani (dohledavaci_osoba_id, dohledavany_osoba_id)
VALUES
    -- Tomáš Dvořák (Senior) dohlíží na Lucii Kučerovou (Junior)
    ((SELECT osoba_id FROM osoba WHERE jmeno = 'Tomáš' AND prijmeni = 'Dvořák'),
     (SELECT osoba_id FROM osoba WHERE jmeno = 'Lucie' AND prijmeni = 'Kučerová')),

    -- Tomáš Dvořák (Senior) dohlíží na Kateřinu Malou (Junior)
    ((SELECT osoba_id FROM osoba WHERE jmeno = 'Tomáš' AND prijmeni = 'Dvořák'),
     (SELECT osoba_id FROM osoba WHERE jmeno = 'Kateřina' AND prijmeni = 'Malá'));

SELECT skoliteldata.jmeno || ' ' || skoliteldata.prijmeni AS supervizor,
       stazistadata.jmeno || ' ' || stazistadata.prijmeni AS dohledovany_doktor
FROM dohledani d
         JOIN doktor skolitel ON d.dohledavaci_osoba_id = skolitel.osoba_id
         join osoba skoliteldata on skolitel.osoba_id = skoliteldata.osoba_id
         JOIN doktor stazista ON d.dohledavany_osoba_id = stazista.osoba_id
         join osoba stazistadata on stazista.osoba_id = stazistadata.osoba_id;

INSERT INTO ukon (nazev_ukonu, popis_ukonu)
VALUES
    ('Odběr krve', 'Základní odběr žilní krve pro laboratorní analýzu.'),
    ('Rentgen hrudníku', 'Radiologické vyšetření plic a srdce k vyloučení patologických nálezů.'),
    ('EKG', 'Elektrokardiografické vyšetření srdeční aktivity.'),
    ('Ultrazvuk břicha', 'Neinvazivní vyšetření orgánů dutiny břišní pomocí ultrazvukových vln.'),
    ('Převaz rány', 'Kontrola, vyčištění a aplikace nového sterilního krytí na chirurgickou ránu.'),
    ('Aplikace Mesocainu', 'Lokální znecitlivění kůže a podkoží injekčním roztokem Mesocain před chirurgickým zákrokem.'),
    ('Infuze Fyziologického roztoku', 'Nitrožilní podání 500ml izotonického roztoku chloridu sodného pro rehydrataci pacienta.'),
    ('Očkování látkou Comirnaty', 'Intramuskulární aplikace vakcíny proti onemocnění COVID-19 do ramenního svalu.'),
    ('Podání Epinefrinu', 'Urgentní nitrosvalová aplikace adrenalinu při rozvoji anafylaktického šoku.'),
    ('Aplikace Insulinu Humulin', 'Podkožní podání krátkodobě působícího lidského inzulínu k regulaci hladiny cukru v krvi.');

select * from ukon;

INSERT INTO kvalifikace_doktora (doktor_id, ukon_id)
VALUES
    -- Tomáš Dvořák (Senior) může dělat urgentní a složité úkony
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Dvořák'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Podání Epinefrinu')),

    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Dvořák'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Aplikace Mesocainu')),

    -- Lucie Kučerová (Junior) může provádět standardní infuze a očkování
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Kučerová'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Infuze Fyziologického roztoku')),

    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Kučerová'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Očkování látkou Comirnaty')),

    -- Kateřina Malá může spravovat insulin
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Malá'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Aplikace Insulinu Humulin')),

    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Malá'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Infuze Fyziologického roztoku'));

SELECT
    o.jmeno,
    o.prijmeni,
    u.nazev_ukonu
FROM kvalifikace_doktora kd
         JOIN osoba o ON kd.doktor_id = o.osoba_id
         JOIN ukon u ON kd.ukon_id = u.ukon_id
ORDER BY o.prijmeni;

INSERT INTO lek (nazev_leku)
VALUES
    ('Mesocain'),
    ('Fyziologický roztok'),
    ('Comirnaty'),
    ('Epinefrin'),
    ('Insulin Humulin'),
    ('Paralen'),
    ('Augmentin'),
    ('Zyrtec');

select * from lek;

INSERT INTO registrovane_leky_pro_ukon (lek_id, ukon_id)
VALUES
    -- Úkon 'Aplikace Mesocainu' vyžaduje lék 'Mesocain'
    ((SELECT lek_id FROM lek WHERE nazev_leku = 'Mesocain'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Aplikace Mesocainu')),

    -- Úkon 'Infuze Fyziologického roztoku' vyžaduje 'Fyziologický roztok'
    ((SELECT lek_id FROM lek WHERE nazev_leku = 'Fyziologický roztok'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Infuze Fyziologického roztoku')),

    -- Úkon 'Očkování látkou Comirnaty' vyžaduje 'Comirnaty'
    ((SELECT lek_id FROM lek WHERE nazev_leku = 'Comirnaty'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Očkování látkou Comirnaty')),

    -- Úkon 'Podání Epinefrinu' vyžaduje 'Epinefrin'
    ((SELECT lek_id FROM lek WHERE nazev_leku = 'Epinefrin'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Podání Epinefrinu')),

    -- Úkon 'Aplikace Insulinu Humulin' vyžaduje 'Insulin Humulin'
    ((SELECT lek_id FROM lek WHERE nazev_leku = 'Insulin Humulin'),
     (SELECT ukon_id FROM ukon WHERE nazev_ukonu = 'Aplikace Insulinu Humulin'));

SELECT
    u.nazev_ukonu,
    l.nazev_leku
FROM registrovane_leky_pro_ukon rlu
         JOIN ukon u ON rlu.ukon_id = u.ukon_id
         JOIN lek l ON rlu.lek_id = l.lek_id;

INSERT INTO zdravotni_karta (cislo_karty, stav)
VALUES
    ('2024/0001', 'aktivní'),
    ('2024/0002', 'aktivní'),
    ('2024/0003', 'aktivní'),
    ('2024/0004', 'aktivní'),
    ('2024/0005', 'aktivní'),
    ('2024/0006', 'aktivní'),
    ('2024/0007', 'aktivní');

INSERT INTO vlastni (zdravotni_karta_id, osoba_id)
VALUES
    -- Propojení Jana Nováka s první kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0001'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Novák' AND jmeno = 'Jan')),

    -- Propojení Marie Svobodové s druhou kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0002'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Svobodová' AND jmeno = 'Marie')),

    -- Propojení Petra Černého s třetí kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0003'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Černý' AND jmeno = 'Petr')),

    -- Propojení Petry Němcové se čtvrtou kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0004'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Němcová' AND jmeno = 'Petra')),

    -- Propojení Jiřího Krále s pátou kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0005'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Král' AND jmeno = 'Jiří')),

    -- Propojení Lukáše Urbana s šestou kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0006'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Urban' AND jmeno = 'Lukáš')),

    -- Propojení Anny Vlčkové se sedmou kartou
    ((SELECT zdravotni_karta_id FROM zdravotni_karta WHERE cislo_karty = '2024/0007'),
     (SELECT osoba_id FROM osoba WHERE prijmeni = 'Vlčková' AND jmeno = 'Anna'));

SELECT
    o.jmeno,
    o.prijmeni,
    zk.cislo_karty,
    zk.stav,
    zk.datum_zalozeni
FROM vlastni v
         JOIN osoba o ON v.osoba_id = o.osoba_id
         JOIN zdravotni_karta zk ON v.zdravotni_karta_id = zk.zdravotni_karta_id;

INSERT INTO chorobopis (
    cislo_chorobopisu,
    datum_od,
    datum_do,
    popis_chorobopisu,
    fk_zdravotni_karta_id
)
VALUES
    -- Jan Novák (Karta ID 1)
    ('CH-2024-001', '2024-01-10', '2024-01-15', 'Hospitalizace pro akutní zánět průdušek, léčba antibiotiky Augmentin.', 1),
    -- Marie Svobodová (Karta ID 2) - Upraveno: datum_do je o den později
    ('CH-2024-002', '2024-02-05', '2024-02-06', 'Preventivní prohlídka a následné pozorování po podání vakcíny Comirnaty.', 2),
    -- Petr Černý (Karta ID 3)
    ('CH-2024-003', '2024-03-12', '2024-03-18', 'Pozorování po otřesu mozku, klidový režim a kontrolní neurologické vyšetření.', 3),
    -- Petra Němcová (Karta ID 4)
    ('CH-2024-004', '2024-04-05', '2024-04-07', 'Krátká hospitalizace pro dehydrataci, podána Infuze Fyziologického roztoku.', 4),
    -- Anna Vlčková (Karta ID 7)
    ('CH-2024-005', '2024-04-10', '2024-04-12', 'Akutní alergická reakce, podání Epinefrinu a 48h monitoring životních funkcí.', 7),
    -- Karta ID 5 (Jiří Král)
    ('CH-2024-006', '2024-04-15', '2024-04-20', 'Podezření na gastroenteritidu, nařízena dieta a rehydratace infuzemi.', 5),
    -- Karta ID 6 (Lukáš Urban)
    ('CH-2024-007', '2024-05-02', '2024-05-05', 'Vyšetření pro chronické bolesti zad, doporučena fyzioterapie a klidový režim.', 6),
    -- Druhý záznam pro Kartu ID 5 (následná kontrola)
    ('CH-2024-008', '2024-05-10', '2024-05-11', 'Kontrolní vyšetření po léčbě zánětu, stav pacienta stabilizován.', 5);


SELECT
    o.jmeno,
    o.prijmeni,
    ch.cislo_chorobopisu,
    ch.datum_od,
    ch.datum_do,
    ch.popis_chorobopisu
FROM chorobopis ch
         JOIN zdravotni_karta zk ON ch.fk_zdravotni_karta_id = zk.zdravotni_karta_id
         JOIN vlastni v ON zk.zdravotni_karta_id = v.zdravotni_karta_id
         JOIN osoba o ON v.osoba_id = o.osoba_id;

SELECT zk.zdravotni_karta_id, zk.cislo_karty, zk.stav
FROM zdravotni_karta zk
         LEFT JOIN chorobopis ch ON zk.zdravotni_karta_id = ch.fk_zdravotni_karta_id
WHERE ch.chorobopis_id IS NULL;

INSERT INTO je_zapsan_do_luzka (fk_pacient_id, fk_luzko_id, datum_od, datum_do)
VALUES
    -- Jan Novák na běžném lůžku (místnost 101)
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Novák'),
     (SELECT luzko_id FROM luzko WHERE fyzicke_cislo = 'L-101-1'),
     '2024-01-10 08:30:00', '2024-01-15 14:00:00'),

    -- Petr Černý na pozorování (místnost 101)
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Černý'),
     (SELECT luzko_id FROM luzko WHERE fyzicke_cislo = 'L-101-2'),
     '2024-03-12 10:00:00', '2024-03-18 09:00:00'),

    -- Anna Vlčková na JIPce (místnost 202)
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Vlčková'),
     (SELECT luzko_id FROM luzko WHERE fyzicke_cislo = 'L-202-JIP'),
     '2024-04-10 16:45:00', '2024-04-12 11:30:00'),

    -- Petra Němcová na intenzivním lůžku (místnost 102)
    ((SELECT osoba_id FROM osoba WHERE prijmeni = 'Němcová'),
     (SELECT luzko_id FROM luzko WHERE fyzicke_cislo = 'L-102-INT'),
     '2024-04-05 07:00:00', '2024-04-07 15:20:00');


SELECT
    o.jmeno,
    o.prijmeni,
    m.cislo_mistnosti,
    l.fyzicke_cislo as cislo_luzka,
    j.datum_od
FROM je_zapsan_do_luzka j
         JOIN osoba o ON j.fk_pacient_id = o.osoba_id
         JOIN luzko l ON j.fk_luzko_id = l.luzko_id
         JOIN mistnost m ON l.fk_mistnost_id = m.mistnost_id;
