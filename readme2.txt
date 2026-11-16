Am implementat persistența datelor într-o bază de date PostgreSQL numită `socialnetwork`,
folosind JDBC.

Aplicația permite în continuare:
- adăugarea și ștergerea utilizatorilor;
- adăugarea și ștergerea prieteniilor;
- afișarea utilizatorilor și a prieteniilor;
- determinarea numărului de comunități;
- determinarea celei mai sociabile comunități.

## 1. Structura bazei de date
Baza de date `socialnetwork` conține două tabele:

**users**
- `id` – BIGINT, cheie primară, generată automat;
- `first_name` – prenumele utilizatorului;
- `last_name` – numele utilizatorului;
- `city` – orașul;
- `email` – adresa de email;
- `birth_date` – data nașterii.

**friendships**
- `id1` – ID-ul primului utilizator;
- `id2` – ID-ul celui de-al doilea utilizator;
- `friends_from` – data și ora începerii prieteniei.

Tabelul `friendships` are cheia primară compusă `(id1, id2)` și constrângerea `CHECK (id1 < id2)`.

## 3. Alegerea ON DELETE CASCADE

Am ales `ON DELETE CASCADE` pentru cheile străine din tabelul `friendships`.

Această variantă garantează că, atunci când un utilizator este șters din baza de date, toate prieteniile sale sunt șterse automat.

Avantaje:
- nu rămân prietenii care fac referire la utilizatori inexistenți;
- integritatea datelor este asigurată de PostgreSQL;
- ștergerea funcționează corect inclusiv dacă utilizatorul este eliminat direct prin SQL.

În `UserService` am păstrat și ștergerea explicită a prieteniilor pentru compatibilitate cu repository-urile FILE și MEMORY.

## 4. Repository-urile JDBC

Am implementat următoarele clase:

- `UtilizatorDbRepository`
- `PrietenieDbRepository`

Ambele implementează interfața generică `Repository<ID, E>`.

Pentru conectarea la PostgreSQL folosesc `DriverManager.getConnection(url, user, password)`.

Operațiile SQL sunt realizate cu `PreparedStatement`, iar valorile sunt transmise prin parametri `?`, fără concatenarea datelor utilizatorului în instrucțiunile SQL.

Resursele `Connection`, `PreparedStatement` și `ResultSet` sunt gestionate folosind `try-with-resources`.

## 5. ID generat automat

În modul DATABASE, ID-ul utilizatorului este generat automat de PostgreSQL prin `GENERATED ALWAYS AS IDENTITY`.

La salvarea unui utilizator, aplicația execută INSERT și obține ID-ul generat folosind `getGeneratedKeys()`.

ID-ul primit este atribuit obiectului `User` și afișat în consolă.

În modurile FILE și MEMORY se păstrează introducerea manuală a ID-ului.

## 6. Contractul Repository

Am adaptat contractul repository-ului conform cerințelor:

- `delete(id)` returnează entitatea ștearsă sau `null` dacă nu există.
- `update(entity)` returnează `null` când actualizarea reușește sau entitatea primită dacă ID-ul nu există.
- Argumentele obligatorii `null` produc `IllegalArgumentException`.
- Entitățile sunt validate înainte de `save()` și `update()`.

## 7. Tratarea excepțiilor

Erorile de tip `SQLException` sunt transformate în `RepositoryException`.

Interfața consolei afișează mesaje de eroare, fără stack trace.

Am testat comportamentul aplicației când serverul PostgreSQL este oprit, apoi am repornit serverul și am verificat accesul la datele salvate.

## 8. Configurare

Tipul repository-ului este selectat din `config.properties`:

- `repository.type=DATABASE` – PostgreSQL;
- `repository.type=FILE` – fișiere text;
- `repository.type=MEMORY` – memorie.

`RepositoryFactory` permite schimbarea modului de stocare fără modificarea codului din service.

Configurația conține și `db.url`, `db.user` și `db.password`.

Fișierul `config.properties`, care conține parola locală, trebuie exclus din Git. Fișierul `config.example.properties` conține configurația exemplificativă, fără o parolă reală.

## 9. Testarea aplicației

Am verificat:
- adăugarea utilizatorilor în PostgreSQL;
- generarea automată a ID-urilor;
- păstrarea datelor după repornirea aplicației;
- schimbarea între DATABASE, FILE și MEMORY;
- afișarea unei erori când PostgreSQL este oprit;
- funcționarea aplicației după repornirea serverului.

Am testat și introducerea unui nume care conține:

`Popescu'); DROP TABLE users; --`

Textul a fost salvat ca nume obișnuit, fără executarea instrucțiunii SQL, datorită utilizării `PreparedStatement`.

Operațiile asupra utilizatorilor și prieteniilor pot fi verificate în pgAdmin prin:

```sql
SELECT * FROM users;
SELECT * FROM friendships;
```

## 10. Documentație și versionare

Clasele și metodele noi sau modificate conțin JavaDoc în limba engleză.

Proiectul este versionat folosind Git.