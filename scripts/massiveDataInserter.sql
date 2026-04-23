-- =============================================================================
-- FULL SEED SCRIPT
-- Fills all tables in the schema with realistic Czech data.
--
-- Order of insertion (respects FK dependencies):
--   1.  Osoba
--   2.  Doktor
--   3.  Pacient
--   4.  Specializace
--   5.  Dohledani
--   6.  Ukon
--   7.  Kvalifikace_doktora
--   8.  Lek
--   9.  Registrovane_leky_pro_ukon
--  10.  Zdravotni_karta
--  11.  Vlastni               (Zdravotni_karta ↔ Pacient)
--  12.  Chorobopis
--  13.  Mistnost
--  14.  Luzko
--  15.  Je_zapsan_do_luzka
--  16.  Provedeni_ukonu       (32 000 rows target)
-- =============================================================================

-- Speed-up: skip FK trigger overhead (requires superuser; comment out if needed)
-- SET session_replication_role = replica;

-- =============================================================================
-- 1. OSOBA  —  2 500 persons  (500 doctors + 2 000 patients)
-- =============================================================================
DO $$
    DECLARE
        male_names   TEXT[] := ARRAY['Adam','Jakub','Jan','Tomáš','Petr','Martin','Lukáš',
            'Ondřej','Pavel','David','Michal','Josef','Jiří','Karel',
            'Radek','Miroslav','Vladimír','Roman','Stanislav','Filip',
            'Marek','Zdeněk','Václav','Aleš','Jaroslav','Bohumil',
            'Libor','Rostislav','Dušan','Miloslav'];
        female_names TEXT[] := ARRAY['Jana','Marie','Eva','Petra','Lucie','Kateřina','Tereza',
            'Martina','Lenka','Veronika','Monika','Alena','Hana',
            'Zuzana','Barbora','Markéta','Ivana','Simona','Renata',
            'Dagmar','Věra','Jitka','Libuše','Vladimíra','Blanka',
            'Šárka','Radka','Ilona','Magdalena','Růžena'];
        male_surnames   TEXT[] := ARRAY['Novák','Svoboda','Novotný','Dvořák','Černý','Procházka',
            'Krejčí','Vlček','Blažek','Pokorný','Marek','Veselý',
            'Horák','Němec','Pospíšil','Šimánek','Kratochvíl',
            'Fiala','Sedláček','Doležal','Kopecký','Král','Růžička',
            'Zeman','Kolář','Hájek','Čermák','Beneš','Hofmann','Urban'];
        female_surnames TEXT[] := ARRAY['Nováková','Svobodová','Novotná','Dvořáková','Černá',
            'Procházková','Krejčová','Vlčková','Blažková','Pokorná',
            'Marková','Veselá','Horáková','Němcová','Pospíšilová',
            'Šimánková','Kratochvílová','Fialová','Sedláčková',
            'Doležalová','Kopecká','Králová','Růžičková','Zemanová',
            'Kolářová','Hájková','Čermáková','Benešová','Hofmannová','Urbanová'];
        cities   TEXT[] := ARRAY['Praha','Brno','Ostrava','Plzeň','Liberec','Olomouc',
            'České Budějovice','Hradec Králové','Pardubice','Zlín',
            'Kladno','Most','Opava','Frýdek-Místek','Havířov',
            'Karviná','Jihlava','Teplice','Děčín','Chomutov',
            'Mladá Boleslav','Prostějov','Přerov','Jablonec nad Nisou',
            'Třebíč','Znojmo','Kolín','Příbram','Cheb','Trutnov'];
        streets  TEXT[] := ARRAY['Hlavní','Nádražní','Školní','Zahradní','Lipová','Polní',
            'Lesní','Luční','Dlouhá','Krátká','Nová','Stará',
            'Masarykova','Husova','Tyršova','Žižkova','Palackého',
            'Komenského','Čechova','Havlíčkova','Mánesova','Riegrova',
            'Smetanova','Dvořákova','Jiráskova','Bezručova','Fügnerova'];
        -- All values must fit varchar(20) — max 20 characters
        hospitals TEXT[] := ARRAY['FN Motol','FN Brno','FN Ostrava','FN Plzeň','FN Olomouc',
            'FN HK','FN Bulovka','NEM Jihlava',
            'NEM Pardubice','KNL Liberec',
            'NEM Zlín','NEM Kladno',NULL,NULL,NULL];
        gender INT; fname TEXT; lname TEXT; dob DATE; ecp TEXT;
        i      INT;
    BEGIN
        FOR i IN 1..2500 LOOP
                gender := (random() * 1)::INT;
                IF gender = 0 THEN
                    fname := male_names  [1 + (random()*(array_length(male_names,  1)-1))::INT];
                    lname := male_surnames[1 + (random()*(array_length(male_surnames,1)-1))::INT];
                ELSE
                    fname := female_names  [1 + (random()*(array_length(female_names,  1)-1))::INT];
                    lname := female_surnames[1 + (random()*(array_length(female_surnames,1)-1))::INT];
                END IF;
                dob := '1940-01-01'::DATE + (random() * ('2005-12-31'::DATE - '1940-01-01'::DATE))::INT;
                -- ECP guaranteed unique: counter-based suffix, no collisions
                ecp := lpad(i::TEXT, 6, '0') || lpad((random()*9999)::INT::TEXT, 4, '0');
                INSERT INTO Osoba (evidencni_cislo_pojistence, jmeno, prijmeni,
                                   datum_narozeni, mesto, ulice, stat, cislo_nemocnice)
                VALUES (ecp, fname, lname, dob,
                        cities  [1 + (random()*(array_length(cities,  1)-1))::INT],
                        streets [1 + (random()*(array_length(streets, 1)-1))::INT]
                            || ' ' || (1+(random()*200)::INT)::TEXT,
                        'Česká republika',
                        hospitals[1 + (random()*(array_length(hospitals,1)-1))::INT]);
            END LOOP;
    END $$;

-- =============================================================================
-- 2. DOKTOR  —  first 500 Osoba rows
-- =============================================================================
DO $$
    DECLARE
        rec RECORD; seq INT := 0;
    BEGIN
        FOR rec IN SELECT osoba_id FROM Osoba ORDER BY osoba_id LIMIT 500 LOOP
                seq := seq + 1;
                BEGIN
                    INSERT INTO Doktor (osoba_id, icl, evidencni_cislo_clk, identifikator_nrzp)
                    VALUES (rec.osoba_id,
                            lpad(seq::TEXT,            6, '0'),
                            lpad((seq+100000)::TEXT,   6, '0'),
                            lpad((seq+100000000)::TEXT,9, '0'));
                EXCEPTION WHEN unique_violation THEN END;
            END LOOP;
    END $$;

-- =============================================================================
-- 3. PACIENT  —  next 2 000 Osoba rows
-- =============================================================================
DO $$
    DECLARE
        rec          RECORD;
        blood_groups TEXT[] := ARRAY['A+','A-','B+','B-','AB+','AB-','0+','0-'];
    BEGIN
        FOR rec IN SELECT osoba_id FROM Osoba ORDER BY osoba_id OFFSET 500 LIMIT 2000 LOOP
                BEGIN
                    INSERT INTO Pacient (osoba_id, krevni_skupina)
                    VALUES (rec.osoba_id,
                            CASE WHEN random() < 0.05 THEN NULL
                                 ELSE blood_groups[1+(random()*7)::INT] END);
                EXCEPTION WHEN unique_violation THEN END;
            END LOOP;
    END $$;

-- =============================================================================
-- 4. SPECIALIZACE  —  1–3 specialisations per doctor
-- =============================================================================
DO $$
    DECLARE
        specs TEXT[] := ARRAY['Všeobecné lékařství','Chirurgie','Interna','Neurologie',
            'Kardiologie','Pediatrie','Psychiatrie','Ortopedie',
            'Onkologie','Radiologie','Anesteziologie','Dermatologie',
            'Gynekologie','Urologie','Oftalmologie','ORL',
            'Endokrinologie','Nefrologie','Revmatologie','Hematologie'];
        rec   RECORD;
        n     INT;
        s     TEXT;
        used  TEXT[];
    BEGIN
        FOR rec IN SELECT osoba_id FROM Doktor LOOP
                n    := 1 + (random() * 2)::INT;   -- 1..3
                used := ARRAY[]::TEXT[];
                FOR i IN 1..n LOOP
                        LOOP
                            s := specs[1+(random()*(array_length(specs,1)-1))::INT];
                            EXIT WHEN NOT (s = ANY(used));
                        END LOOP;
                        used := array_append(used, s);
                        BEGIN
                            INSERT INTO Specializace (fk_osoba_id, specializace) VALUES (rec.osoba_id, s);
                        EXCEPTION WHEN unique_violation THEN END;
                    END LOOP;
            END LOOP;
    END $$;

-- =============================================================================
-- 5. DOHLEDANI  —  ~200 supervision pairs (senior → junior doctors)
-- =============================================================================
DO $$
    DECLARE
        dok_ids INT[];
        sup_id  INT;
        jun_id  INT;
        i       INT;
    BEGIN
        SELECT ARRAY(SELECT osoba_id FROM Doktor ORDER BY osoba_id) INTO dok_ids;
        FOR i IN 1..200 LOOP
                sup_id := dok_ids[1 + (random()*(array_length(dok_ids,1)-1))::INT];
                LOOP
                    jun_id := dok_ids[1 + (random()*(array_length(dok_ids,1)-1))::INT];
                    EXIT WHEN jun_id <> sup_id;
                END LOOP;
                BEGIN
                    INSERT INTO Dohledani (dohledavaci_osoba_id, dohledavany_osoba_id)
                    VALUES (sup_id, jun_id);
                EXCEPTION WHEN unique_violation THEN END;
            END LOOP;
    END $$;

-- =============================================================================
-- 6. UKON  —  50 medical procedures
-- =============================================================================
INSERT INTO Ukon (nazev_ukonu, popis_ukonu) VALUES
                                                ('Vstupní vyšetření',           'Komplexní vstupní vyšetření pacienta včetně anamnézy a fyzického vyšetření.'),
                                                ('Kontrolní vyšetření',         'Pravidelné kontrolní vyšetření stavu pacienta.'),
                                                ('Odběr krve',                  'Odběr venózní krve pro laboratorní analýzu.'),
                                                ('EKG',                         'Elektrokardiografické vyšetření srdeční aktivity.'),
                                                ('RTG hrudníku',                'Rentgenové vyšetření hrudní oblasti.'),
                                                ('Ultrazvuk břicha',            'Ultrasonografické vyšetření břišních orgánů.'),
                                                ('Měření krevního tlaku',       'Neinvazivní měření arteriálního krevního tlaku.'),
                                                ('Očkování',                    'Aplikace vakcíny dle očkovacího plánu.'),
                                                ('Převaz rány',                 'Ošetření a převaz chirurgické nebo traumatické rány.'),
                                                ('Injekce intramuskulární',     'Aplikace léku intramuskulární cestou.'),
                                                ('Infuze',                      'Nitrožilní podání léků nebo roztoků.'),
                                                ('Spirometrie',                 'Vyšetření plicních funkcí spirometrem.'),
                                                ('Holterovo monitorování',      'Dlouhodobé ambulantní monitorování EKG.'),
                                                ('Gastroskopie',                'Endoskopické vyšetření horní části trávicí soustavy.'),
                                                ('Kolonoskopie',                'Endoskopické vyšetření tlustého střeva.'),
                                                ('Odběr moči',                  'Sběr vzorku moči pro laboratorní analýzu.'),
                                                ('Glykémie nalačno',            'Stanovení hladiny krevního cukru nalačno.'),
                                                ('Testy alergie',               'Kožní nebo krevní testy k určení alergenů.'),
                                                ('Fyzioterapie',                'Terapeutická cvičení a procedury vedené fyzioterapeutem.'),
                                                ('Psychologická konzultace',    'Konzultace s klinickým psychologem.'),
                                                ('Dermatologické vyšetření',    'Vyšetření kůže, nehtů a vlasů.'),
                                                ('Oftalmologické vyšetření',    'Vyšetření zraku a očního pozadí.'),
                                                ('Audiometrie',                 'Vyšetření sluchu audiometrem.'),
                                                ('Neurologické vyšetření',      'Komplexní neurologické vyšetření.'),
                                                ('Ortopedická konzultace',      'Konzultace ortopedického specialisty.'),
                                                ('Biopsie',                     'Odběr tkáňového vzorku pro histologické vyšetření.'),
                                                ('CT vyšetření',                'Počítačová tomografie vybrané oblasti.'),
                                                ('MRI vyšetření',               'Magnetická rezonance vybrané oblasti.'),
                                                ('PET scan',                    'Pozitronová emisní tomografie.'),
                                                ('Mammografie',                 'Rentgenové vyšetření prsní žlázy.'),
                                                ('DEXA sken',                   'Denzitometrie – měření hustoty kostí.'),
                                                ('Kardiologická konzultace',    'Konzultace kardiologa.'),
                                                ('Onkologická konzultace',      'Konzultace onkologa.'),
                                                ('Endokrinologická konzultace', 'Konzultace endokrinologa.'),
                                                ('Nefrologická konzultace',     'Konzultace nefrologa.'),
                                                ('Urologická konzultace',       'Konzultace urologa.'),
                                                ('Gynekologické vyšetření',     'Preventivní gynekologické vyšetření.'),
                                                ('Pap test',                    'Cytologický stěr z děložního čípku.'),
                                                ('PSA test',                    'Krevní test na prostatický specifický antigen.'),
                                                ('Rehabilitace',                'Fyzikální rehabilitační program.'),
                                                ('Logopedická terapie',         'Terapie poruch řeči a komunikace.'),
                                                ('Nutriční poradenství',        'Konzultace s nutričním terapeutem.'),
                                                ('Pracovní lékařství',          'Preventivní prohlídka v rámci pracovního lékařství.'),
                                                ('Anesteziologická konzultace', 'Předoperační konzultace anesteziologa.'),
                                                ('Chirurgická konzultace',      'Konzultace chirurga před plánovaným výkonem.'),
                                                ('Koagulační vyšetření',        'Krevní test na parametry srážlivosti.'),
                                                ('CRP a zánětlivé markery',     'Stanovení C-reaktivního proteinu a dalších markerů zánětu.'),
                                                ('Terapie bolesti',             'Konzultace a léčba chronické bolesti.'),
                                                ('Psychiatrická konzultace',    'Diagnostická a terapeutická konzultace psychiatra.'),
                                                ('Výplach ucha',                'Laváž zevního zvukovodu při cerumenu.')
ON CONFLICT (nazev_ukonu) DO NOTHING;

-- =============================================================================
-- 7. KVALIFIKACE_DOKTORA  —  each doctor qualified for 3–8 procedures
-- =============================================================================
DO $$
    DECLARE
        dok_ids  INT[];
        ukon_ids INT[];
        n        INT;
        used     INT[];
        u        INT;
    BEGIN
        SELECT ARRAY(SELECT osoba_id FROM Doktor ORDER BY osoba_id) INTO dok_ids;
        SELECT ARRAY(SELECT ukon_id  FROM Ukon   ORDER BY ukon_id)  INTO ukon_ids;
        FOR i IN 1..array_length(dok_ids,1) LOOP
                n    := 3 + (random()*5)::INT;
                used := ARRAY[]::INT[];
                FOR j IN 1..n LOOP
                        LOOP
                            u := ukon_ids[1+(random()*(array_length(ukon_ids,1)-1))::INT];
                            EXIT WHEN NOT (u = ANY(used));
                        END LOOP;
                        used := array_append(used, u);
                        BEGIN
                            INSERT INTO Kvalifikace_doktora (doktor_id, ukon_id) VALUES (dok_ids[i], u);
                        EXCEPTION WHEN unique_violation THEN END;
                    END LOOP;
            END LOOP;
    END $$;

-- =============================================================================
-- 8. LEK  —  60 common medications
-- =============================================================================
INSERT INTO Lek (nazev_leku) VALUES
                                 ('Paralen 500mg'),('Ibuprofen 400mg'),('Amoxicilin 500mg'),('Augmentin 625mg'),
                                 ('Aspirin 100mg'),('Warfarin 5mg'),('Metformin 500mg'),('Atorvastatin 20mg'),
                                 ('Lisinopril 10mg'),('Amlodipine 5mg'),('Omeprazol 20mg'),('Pantoprazol 40mg'),
                                 ('Furosemid 40mg'),('Spironolakton 25mg'),('Bisoprolol 5mg'),('Carvedilol 12,5mg'),
                                 ('Ramipril 5mg'),('Losartan 50mg'),('Simvastatin 40mg'),('Rosuvastatin 10mg'),
                                 ('Metoprolol 50mg'),('Digoxin 0,25mg'),('Levothyroxin 50mcg'),('Prednison 5mg'),
                                 ('Methylprednisolon 4mg'),('Dexamethason 4mg'),('Cetirizin 10mg'),('Loratadin 10mg'),
                                 ('Montelukast 10mg'),('Salbutamol 100mcg'),('Budesonid 200mcg'),('Tiotropium 18mcg'),
                                 ('Sertralin 50mg'),('Escitalopram 10mg'),('Fluoxetin 20mg'),('Venlafaxin 75mg'),
                                 ('Alprazolam 0,25mg'),('Diazepam 5mg'),('Zolpidem 10mg'),('Tramadol 50mg'),
                                 ('Kodein 30mg'),('Morfin 10mg'),('Gabapentin 300mg'),('Pregabalin 75mg'),
                                 ('Methotrexat 2,5mg'),('Hydroxychlorochin 200mg'),('Adalimumab inj.'),
                                 ('Infliximab inf.'),('Insulin Glargin'),('Insulin Aspart'),
                                 ('Glimepirid 2mg'),('Sitagliptin 100mg'),('Empagliflozin 10mg'),
                                 ('Alopurinol 300mg'),('Kolchicin 0,5mg'),('Ciprofloxacin 500mg'),
                                 ('Doxycyklin 100mg'),('Flukonazol 150mg'),('Acyclovir 400mg'),
                                 ('Enoxaparin 40mg inj.')
ON CONFLICT (nazev_leku) DO NOTHING;

-- =============================================================================
-- 9. REGISTROVANE_LEKY_PRO_UKON  —  2–5 drugs per procedure
-- =============================================================================
DO $$
    DECLARE
        lek_ids  INT[];
        ukon_ids INT[];
        n        INT;
        used     INT[];
        l        INT;
    BEGIN
        SELECT ARRAY(SELECT lek_id  FROM Lek  ORDER BY lek_id)  INTO lek_ids;
        SELECT ARRAY(SELECT ukon_id FROM Ukon ORDER BY ukon_id) INTO ukon_ids;
        FOR i IN 1..array_length(ukon_ids,1) LOOP
                n    := 2 + (random()*3)::INT;
                used := ARRAY[]::INT[];
                FOR j IN 1..n LOOP
                        LOOP
                            l := lek_ids[1+(random()*(array_length(lek_ids,1)-1))::INT];
                            EXIT WHEN NOT (l = ANY(used));
                        END LOOP;
                        used := array_append(used, l);
                        BEGIN
                            INSERT INTO Registrovane_leky_pro_ukon (lek_id, ukon_id)
                            VALUES (l, ukon_ids[i]);
                        EXCEPTION WHEN unique_violation THEN END;
                    END LOOP;
            END LOOP;
    END $$;

-- =============================================================================
-- 10. ZDRAVOTNI_KARTA  —  2 000 cards (one per patient)
-- =============================================================================
DO $$
    DECLARE
        i   INT;
        seq INT := 0;
    BEGIN
        FOR i IN 1..2000 LOOP
                seq := seq + 1;
                BEGIN
                    INSERT INTO Zdravotni_karta (cislo_karty, stav, datum_zalozeni)
                    VALUES (
                               'ZK-' || lpad(seq::TEXT, 6, '0'),
                               (ARRAY['aktivní','neaktivní','archivovaný']::stav_enum[])[1+(random()*2)::INT],
                               CURRENT_DATE - (random()*3650)::INT
                           );
                EXCEPTION WHEN unique_violation THEN END;
            END LOOP;
    END $$;

-- =============================================================================
-- 11. VLASTNI  —  assign one health card per patient
-- =============================================================================
DO $$
    DECLARE
        pac_ids  INT[];
        karta_ids INT[];
        i        INT;
    BEGIN
        SELECT ARRAY(SELECT osoba_id          FROM Pacient        ORDER BY osoba_id)          INTO pac_ids;
        SELECT ARRAY(SELECT zdravotni_karta_id FROM Zdravotni_karta ORDER BY zdravotni_karta_id) INTO karta_ids;
        FOR i IN 1..LEAST(array_length(pac_ids,1), array_length(karta_ids,1)) LOOP
                BEGIN
                    INSERT INTO Vlastni (zdravotni_karta_id, osoba_id)
                    VALUES (karta_ids[i], pac_ids[i]);
                EXCEPTION WHEN unique_violation THEN END;
            END LOOP;
    END $$;

-- =============================================================================
-- 12. CHOROBOPIS  —  1–4 case files per health card
-- =============================================================================
DO $$
    DECLARE
        karta_ids INT[];
        n         INT;
        seq       INT := 0;
        d_od      DATE;
        d_do      DATE;
        popisy    TEXT[] := ARRAY[
            'Pacient hospitalizován s akutními bolestmi břicha. Provedena diagnostika a léčba.',
            'Chronické onemocnění dýchacích cest. Zahájena dlouhodobá terapie.',
            'Pooperační sledování po elektivním výkonu. Hojení bez komplikací.',
            'Kardiovaskulární příhoda. Stabilizace stavu a zahájení farmakoterapie.',
            'Metabolické onemocnění v kompenzaci. Úprava medikace.',
            'Infekční onemocnění léčeno antibiotiky. Klinické zlepšení po 5 dnech.',
            'Neurologická symptomatologie vyšetřena. Nasazena antiepileptická terapie.',
            'Ortopedická diagnóza. Konzervativní léčba a rehabilitace.',
            'Psychiatrická hospitalizace. Stabilizace psychického stavu.',
            'Onkologická diagnóza. Zahájena chemoterapie dle protokolu.'
            ];
    BEGIN
        SELECT ARRAY(SELECT zdravotni_karta_id FROM Zdravotni_karta ORDER BY zdravotni_karta_id)
        INTO karta_ids;
        FOR i IN 1..array_length(karta_ids,1) LOOP
                n := 1 + (random()*3)::INT;
                FOR j IN 1..n LOOP
                        seq  := seq + 1;
                        d_od := '2015-01-01'::DATE + (random()*('2024-12-31'::DATE-'2015-01-01'::DATE))::INT;
                        d_do := CASE WHEN random() < 0.3 THEN NULL
                                     ELSE d_od + (1 + (random()*365)::INT) END;
                        IF d_do > CURRENT_DATE THEN d_do := NULL; END IF;
                        BEGIN
                            INSERT INTO Chorobopis (cislo_chorobopisu, datum_od, datum_do,
                                                    popis_chorobopisu, fk_zdravotni_karta_id)
                            VALUES ('CH-' || lpad(seq::TEXT,7,'0'), d_od, d_do,
                                    popisy[1+(random()*(array_length(popisy,1)-1))::INT],
                                    karta_ids[i]);
                        EXCEPTION WHEN unique_violation THEN END;
                    END LOOP;
            END LOOP;
    END $$;

-- =============================================================================
-- 13. MISTNOST  —  120 rooms across departments
-- =============================================================================
DO $$
    DECLARE
        departments  oddeleni_enum[]       := ARRAY['chirurgie','interna','neurologie',
            'kardiologie','pediatrie','psychiatrie',
            'ortopedie','onkologie']::oddeleni_enum[];
        colors       barva_mistnosti_enum[] := ARRAY['fialova','zelena','modra','bila']::barva_mistnosti_enum[];
        seq          INT := 0;
        dep          oddeleni_enum;
        col          barva_mistnosti_enum;
    BEGIN
        FOREACH dep IN ARRAY departments LOOP
                FOR room_num IN 1..15 LOOP
                        seq := seq + 1;
                        col := colors[1+(random()*(array_length(colors,1)-1))::INT];
                        BEGIN
                            INSERT INTO Mistnost (cislo_mistnosti, barva_mistnosti, oddeleni)
                            VALUES (dep::TEXT || '-' || lpad(room_num::TEXT,3,'0'), col, dep);
                        EXCEPTION WHEN unique_violation THEN END;
                    END LOOP;
            END LOOP;
    END $$;

-- =============================================================================
-- 14. LUZKO  —  3–6 beds per room
-- =============================================================================
DO $$
    DECLARE
        room_rec   RECORD;
        n          INT;
        importance dulezitost_luzka_enum;
        w          FLOAT;
    BEGIN
        FOR room_rec IN SELECT mistnost_id FROM Mistnost LOOP
                n := 3 + (random()*3)::INT;
                FOR b IN 1..n LOOP
                        w := random();
                        importance := CASE
                                          WHEN w < 0.70 THEN 'běžná'::dulezitost_luzka_enum
                                          WHEN w < 0.90 THEN 'intenzivní'::dulezitost_luzka_enum
                                          ELSE               'jednotka_intenzivní_péče'::dulezitost_luzka_enum
                            END;
                        BEGIN
                            INSERT INTO Luzko (fyzicke_cislo, fk_mistnost_id, dulezitost_luzka)
                            VALUES (room_rec.mistnost_id::TEXT || '-B' || b, room_rec.mistnost_id, importance);
                        EXCEPTION WHEN unique_violation THEN END;
                    END LOOP;
            END LOOP;
    END $$;

-- =============================================================================
-- 15. JE_ZAPSAN_DO_LUZKA  —  ~1 500 admissions
-- =============================================================================
DO $$
    DECLARE
        pac_ids  INT[];
        luzko_ids INT[];
        p_id     INT;
        l_id     INT;
        d_od     TIMESTAMP;
        d_do     TIMESTAMP;
        i        INT;
    BEGIN
        SELECT ARRAY(SELECT osoba_id FROM Pacient ORDER BY osoba_id) INTO pac_ids;
        SELECT ARRAY(SELECT luzko_id FROM Luzko   ORDER BY luzko_id) INTO luzko_ids;
        FOR i IN 1..1500 LOOP
                p_id := pac_ids [1+(random()*(array_length(pac_ids, 1)-1))::INT];
                l_id := luzko_ids[1+(random()*(array_length(luzko_ids,1)-1))::INT];
                d_od := (CURRENT_TIMESTAMP - ((random()*1825)::INT || ' days')::INTERVAL
                    - ((random()*23 )::INT || ' hours')::INTERVAL);
                d_do := CASE
                            WHEN random() < 0.15 THEN NULL   -- still admitted
                            ELSE d_od + ((1 + (random()*30)::INT) || ' days')::INTERVAL
                    END;
                IF d_do > CURRENT_TIMESTAMP THEN d_do := NULL; END IF;
                BEGIN
                    INSERT INTO Je_zapsan_do_luzka (fk_pacient_id, fk_luzko_id, datum_od, datum_do)
                    VALUES (p_id, l_id, d_od, d_do);
                EXCEPTION WHEN unique_violation THEN END;
            END LOOP;
    END $$;

-- =============================================================================
-- 16. PROVEDENI_UKONU  —  32 000 rows
-- =============================================================================
DO $$
    DECLARE
        doktor_ids  INT[];
        pacient_ids INT[];
        ukon_ids    INT[];
        d_id        INT;
        p_id        INT;
        u_id        INT;
        appt_date   DATE;
        appt_time   TIME;
        inserted    INT := 0;
        attempts    INT := 0;
        nd          INT;
        np          INT;
        nu          INT;
    BEGIN
        SELECT ARRAY(SELECT osoba_id FROM Doktor  ORDER BY osoba_id) INTO doktor_ids;
        SELECT ARRAY(SELECT osoba_id FROM Pacient ORDER BY osoba_id) INTO pacient_ids;
        SELECT ARRAY(SELECT ukon_id  FROM Ukon    ORDER BY ukon_id)  INTO ukon_ids;

        nd := array_length(doktor_ids,  1);
        np := array_length(pacient_ids, 1);
        nu := array_length(ukon_ids,    1);

        RAISE NOTICE 'Pool: % doctors, % patients, % procedures', nd, np, nu;

        -- Insert with on-conflict-do-nothing to avoid exception overhead
        -- Use a deterministic spread first: assign each row a unique base slot,
        -- then randomise the time component to avoid clashes.
        FOR n IN 1..32000 LOOP
                attempts := attempts + 1;
                d_id      := doktor_ids [1 + (random()*(nd-1))::INT];
                p_id      := pacient_ids[1 + (random()*(np-1))::INT];
                u_id      := ukon_ids   [1 + (random()*(nu-1))::INT];
                -- Spread dates uniformly across 5 years (1825 days)
                appt_date := CURRENT_DATE - (((n-1) * 1825 / 32000) + (random()*5)::INT)::INT;
                IF appt_date > CURRENT_DATE THEN appt_date := CURRENT_DATE; END IF;
                -- 07:00–17:00 in 5-min slots (120 slots) — more granularity = fewer collisions
                appt_time := '07:00'::TIME + (((random()*119)::INT) * INTERVAL '5 minutes');

                INSERT INTO Provedeni_ukonu (fk_pacient_id, fk_doktor_id, fk_ukon_id, datum, cas)
                VALUES (p_id, d_id, u_id, appt_date, appt_time)
                ON CONFLICT DO NOTHING;

                GET DIAGNOSTICS inserted = ROW_COUNT;
                inserted := inserted + (CASE WHEN inserted > 0 THEN 0 ELSE 0 END); -- suppress unused warning
            END LOOP;

        -- Top up any rows lost to conflicts
        WHILE (SELECT COUNT(*) FROM Provedeni_ukonu) < 32000 AND attempts < 500000 LOOP
                attempts  := attempts + 1;
                d_id      := doktor_ids [1 + (random()*(nd-1))::INT];
                p_id      := pacient_ids[1 + (random()*(np-1))::INT];
                u_id      := ukon_ids   [1 + (random()*(nu-1))::INT];
                appt_date := CURRENT_DATE - (random()*1825)::INT;
                appt_time := '07:00'::TIME + (((random()*119)::INT) * INTERVAL '5 minutes');
                INSERT INTO Provedeni_ukonu (fk_pacient_id, fk_doktor_id, fk_ukon_id, datum, cas)
                VALUES (p_id, d_id, u_id, appt_date, appt_time)
                ON CONFLICT DO NOTHING;
            END LOOP;

        RAISE NOTICE 'Provedeni_ukonu final count: %', (SELECT COUNT(*) FROM Provedeni_ukonu);
    END $$;

-- =============================================================================
-- Re-enable FK triggers
-- =============================================================================
-- SET session_replication_role = DEFAULT;