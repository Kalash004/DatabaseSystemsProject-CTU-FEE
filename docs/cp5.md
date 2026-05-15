# 1. Inheritance Joins (@PrimaryKeyJoinColumn)
InheritanceType.JOINED strategy on Osoba, the child tables are linked back to the parent table via their primary keys.

- Doktor: Joined to Osoba via @PrimaryKeyJoinColumn(name = "osoba_id")
- Pacient: Joined to Osoba via @PrimaryKeyJoinColumn(name = "osoba_id")


# 2. Many-to-One Joins (@ManyToOne + @JoinColumn)
These are standard foreign key relationships where the current entity's table holds a column referencing the primary key of another table.

- Chorobopis $\rightarrow$ ZdravotniKarta (via fk_zdravotni_karta_id)
- JeZapsanDoLuzka $\rightarrow$ Pacient (via fk_pacient_id)
- JeZapsanDoLuzka $\rightarrow$ Luzko (via fk_luzko_id)
- Luzko $\rightarrow$ Mistnost (via fk_mistnost_id)
- ProvedeniUkonu $\rightarrow$ Pacient (via fk_pacient_id)
- ProvedeniUkonu $\rightarrow$ Doktor (via fk_doktor_id)
- ProvedeniUkonu $\rightarrow$ Ukon (via fk_ukon_id)
- Specializace $\rightarrow$ Doktor (via fk_osoba_id)

# 3. One-to-Many Joins (@OneToMany + @JoinColumn)
These are unidirectional relationships where the current entity fetches a collection of children using a foreign key located in the child's table.

- Pacient $\rightarrow$ JeZapsanDoLuzka collection (Joined via fk_pacient_id in the je_zapsan_do_luzka table)
- Pacient $\rightarrow$ ProvedeniUkonu collection (Joined via fk_pacient_id in the provedeni_ukonu table)

# 4. One-to-One via Join Table (@OneToOne + @JoinTable)
Instead of a direct foreign key column, this uses an intermediate junction table to map a strict 1:1 relationship between two entities.

- Pacient $\leftrightarrow$ ZdravotniKarta:
- Joined via the vlastni table.
- Join columns: osoba_id (unique) and zdravotni_karta_id (unique).
# 5. Many-to-Many Joins (@ManyToMany + @JoinTable)
These mappings use a dedicated junction/link table to resolve Many-to-Many relationships.

- Doktor $\leftrightarrow$ Doktor (Self-referencing for Supervisors/Supervisees):
    - Joined via the dohledani table.
    - Join columns: dohledavaci_osoba_id and dohledavany_osoba_id.
    - Note: This also features a mappedBy = "dohledavani" inverse relationship.
- Doktor $\leftrightarrow$ Ukon (Doctor Qualifications):
    - Joined via the kvalifikace_doktora table.
    - Join columns: doktor_id and ukon_id.
- Ukon $\leftrightarrow$ Lek (Registered Medications for Procedures):
    - Joined via the registrovane_leky_pro_ukon table.
    - Join columns: ukon_id and lek_id.


# JPA MAPPINGS 

1. @ManyToOne (Many-to-One)
    Concept: Many rows in "Table A" belong to one specific row in "Table B". Everyday Example: Many Employees belong to one Department. How it works in the Database: The "Many" table holds a Foreign Key pointing to the "One" table. For example, the Employee table has a department_id column. How it works in JPA: The entity that holds the foreign key gets the @ManyToOne annotation. This is the most common and safest relationship in JPA.

    java
    @ManyToOne
    @JoinColumn(name = "department_id") // The foreign key column in the Employee table
    private Department department;



2. @OneToMany (One-to-Many)
    Concept: This is simply the exact reverse of @ManyToOne. One row in "Table A" contains many rows in "Table B". Everyday Example: One Department has many Employees. How it works in the Database: The database doesn't actually change! There is no "list of employees" column in the Department table. The database still just relies on the department_id foreign key inside the Employee table. How it works in JPA: Because the database doesn't change, we use mappedBy to tell JPA: "Hey, look at the department property in the Employee class to figure out how this is mapped."

    java
    @OneToMany(mappedBy = "department") 
    private List<Employee> employees;
    
    (Note: If you use @OneToMany without mappedBy and instead use a @JoinColumn directly, like you did in Pacient.java, JPA creates a "unidirectional One-To-Many" relationship where the parent updates the child's foreign key).



3. @OneToOne (One-to-One)
    Concept: One row in "Table A" belongs to exactly one row in "Table B" (and vice versa). Everyday Example: A User has one UserProfile. How it works in the Database: One table holds a foreign key to the other, but importantly, that foreign key must be UNIQUE. This guarantees that no two Users can share the same UserProfile. How it works in JPA: One side holds the @JoinColumn, and the other side holds the mappedBy.

    java
    @OneToOne
    @JoinColumn(name = "profile_id", unique = true) // Foreign key
    private UserProfile profile;



4. @ManyToMany (Many-to-Many)
    Concept: Many rows in "Table A" can relate to many rows in "Table B". Everyday Example: Students and Courses. A student takes many courses, and a course has many students. How it works in the Database: Relational databases cannot natively handle Many-to-Many relationships. You must create a third table (a "Join Table" or "Junction Table") that sits in the middle. This table holds two foreign keys: student_id and course_id. How it works in JPA: One side must define the @JoinTable describing this middle table. The other side (if you want the relationship to be bidirectional) uses mappedBy.

    java
    // On the Student side (The "Owning" side)
    @ManyToMany
    @JoinTable(
    name = "student_course_enrollments", 
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses;
    // On the Course side (The "Inverse" side)
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students;
    

    
Summary: The Golden Rule of JPA Mappings
When trying to figure out which mapping to use, always ask yourself: "Where does the Foreign Key go in the database?"

If the foreign key is in this entity's table $\rightarrow$ @ManyToOne (or the owning side of a @OneToOne).
If the foreign key is in the other entity's table $\rightarrow$ @OneToMany (or the mappedBy side of a @OneToOne).
If the foreign keys are in a completely separate 3rd table $\rightarrow$ @ManyToMany.