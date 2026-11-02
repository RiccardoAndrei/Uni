1 . RepositoryFactory : are rolul de a crea repository-urile corespunzatoare tipului de persistenta ales.
    Tipul de persistenta este ales in config.properties.
    Avem 3 tipuri :
    1. MEMORY - datele sunt pastrate in memorie folosind InMemoryRepository
    2. FILE - datele sunt pastrate in fisiere text , UtilizatorFileRepository si PrietenieFIleRepository
    3. DATABASE - datele sunt in baze de date

    Factory-ul conține metodele:
    createUtilizatorRepository(validator)
    createPrietenieRepository(validator)
    Aceste metode returnează obiecte prin interfața generică Repository, nu prin tipurile concrete.
    Avantajul este că Main, clasele din service și interfața cu utilizatorul nu trebuie modificate atunci
    când schimbăm tipul de persistență.

    Pentru trecerea la persistenta la baza de date trebuie implementate metodele claselor :
        UtilizatorDbRepository
        PrietenieDbRepository
    Si trebuie sa schimbam in config.properties repository = DATABASE

2 . Citirea și scrierea fișierelor sunt gestionate de clasa AbstractFileRepository.
    Fișier inexistent : Dacă fișierul de utilizatori sau de prietenii nu există la pornirea aplicației,
    repository-ul corespunzător pornește gol.
    Aplicația nu se oprește din cauza absenței fișierului.
    La prima operație de salvare reușită, fișierul este creat automat.

    Linie coruptă sau invalidă :
    La încărcarea datelor, fiecare linie este transformată într-o entitate și verificată prin validator.
    Dacă o linie conține date invalide, de exemplu un ID care nu este numeric, o dată incorectă sau câmpuri
    lipsă, aceasta este ignorată.

    Pentru tratarea erorilor folosim ValidationException și RepositoryException. Erorile de citire,
    scriere și conversie sunt raportate prin excepții care păstrează cauza inițială atunci când este necesar.

3. Principiile SOLID utilizate

    SRP – Single Responsibility Principle
    Principiul spune că fiecare clasă trebuie să aibă o responsabilitate principală.
    În proiect:

    User si Friendship reprezintă entitatile domeniului.
    UserService gestionează operatiile asupra utilizatorilor.
    FriendshipService gestioneaza prieteniile.
    CommunityService implementeaza algoritmii pentru comunitati.
    AbstractFileRepository gestionează persistența în fișiere.
    ConsoleUI gestionează interacțiunea cu utilizatorul.
    RepositoryFactory este responsabilă de crearea repository-urilor.

    OCP – Open/Closed Principle
    Principiul spune că o componentă trebuie să fie deschisă pentru extindere, dar închisă pentru modificare.
    În proiect folosim interfața Repository și interfața Validator.
    Putem crea noi implementări ale acestora fără să modificăm codul serviciilor.
    De exemplu, putem introduce un validator cu reguli mai stricte fără să modificăm InMemoryRepository.
    De asemenea, putem implementa repository-uri pentru baza de date fără să modificăm serviciile existente.

    DIP – Dependency Inversion Principle
    Principiul spune că modulele de nivel înalt trebuie să depindă de abstracții, nu de implementări concrete.
    În proiect, clasele UserService, FriendshipService și CommunityService folosesc interfața Repository.
    Aceste clase nu depind direct de InMemoryRepository, UtilizatorFileRepository sau de repository-urile
    pentru baza de date.
    Repository-urile primesc și validatorii prin interfața Validator.
    Prin urmare, implementările pot fi schimbate fără modificarea logicii aplicației.


    LSP – Liskov Substitution Principle
    Principiul spune că o clasă derivată trebuie să poată înlocui clasa de bază fără să modifice
    comportamentul așteptat al programului.
    InMemoryRepository și AbstractFileRepository respectă contractul definit de interfața Repository.
    De exemplu, apelul delete(null) trebuie să arunce IllegalArgumentException, indiferent de implementarea folosită.
    Entity și Tuple implementează corect metodele equals() și hashCode(), pentru identificarea și compararea entităților.

4. Șabloane de proiectare utilizate

    Factory Pattern
    Este utilizat în clasa RepositoryFactory.
    Factory-ul creează repository-urile în funcție de configurarea aplicației, centralizând alegerea
    implementărilor concrete.

    Template Method Pattern
    Este utilizat în clasa AbstractFileRepository.
    Această clasă conține algoritmul comun pentru încărcarea datelor și salvarea lor în fișiere text.
    Clasele UtilizatorFileRepository și PrietenieFileRepository implementează doar
    metodele specifice conversiei:
    extractEntity(String line) – transformă o linie într-o entitate.
    createEntityAsString(E entity) – transformă o entitate într-o linie de text.
    Astfel, codul pentru citirea și scrierea fișierelor nu este duplicat în subclase.
    La încărcarea datelor se utilizează super.save(entity), pentru a valida și salva entitățile în memorie
    fără rescrierea fișierului în timpul citirii.

    Strategy Pattern
    Este utilizat pentru validarea entităților.
    Interfața generică Validator<T> definește metoda validate(T entity).
    Implementările sunt:
    UserValidator
    FriendshipValidator
    FriendshipWithUsersValidator
    Validatorul este transmis prin constructor, astfel încât regulile de validare pot fi schimbate fără
    modificarea repository-ului.

    Decorator Pattern
    Conceptul de Decorator apare în operațiile de intrare/ieșire prin utilizarea BufferedReader și
    BufferedWriter.
    Aceste clase adaugă funcționalități de buffering peste operațiile de citire și scriere,
    îmbunătățind eficiența.
    În proiect folosim Files.newBufferedReader() și Files.newBufferedWriter(), împreună cu
    try-with-resources, pentru închiderea automată a resurselor.
    Un alt exemplu este FriendshipWithUsersValidator, care primește un Validator<Friendship> și
    adaugă verificarea existenței utilizatorilor, delegând validarea de bază validatorului primit.