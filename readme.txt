## 1. Tabel înainte/după – bucle înlocuite cu Stream API

CLASA               METODA               LAB3                                   LAB4
1.UserService       removeUser()        for pentru parcurgerea prieteniilor     stream() , filter() ,
                                        si identificarea celor care trebuie     collect() , forEach()
                                        sterse

2.AbstractFileRepository  loadFromFile() while cu reader.readLine() pentru      Files.lines()
                                         citirea liniilor                       collect(),
                                                                                IntStream.range(),filter().
                                                                                forEach()
3.AbstractFileRepository  writeToFile()  for pentru transformarea entitatiilor  stream() , map() , collect,
                                         in linii de text                       Files.write()

4.CommunityService     buildGraph()      Doua bucle for : una pentru utilizatori stream() , map() , filter
                                         si una pentru prietenii                 collect(toMap) , si
                                                                                 collect(toSet)

5.CommunityService     longestPath()    for pentru explorarea vecinilor         stream() , filter() ,
                                        si calcularea drumului maxim            mapToInt() , max()

## 2. Operații intermediare și terminale

Un pipeline de stream pornește de la o sursă de date, aplică operații intermediare și se încheie cu operație terminală.

**Operații intermediare:**
- `filter()` – selectează elementele care respectă o condiție.
- `map()` – transformă fiecare element.
- `flatMap()` – transformă și aplatizează rezultatele.
- `sorted()` – sortează elementele.
- `distinct()` – elimină duplicatele.

**Operații terminale:**
- `collect()` – construiește o colecție.
- `count()` – numără elementele.
- `max()` / `min()` – determină un element extrem.
- `reduce()` – combină elementele.
- `forEach()` – execută o acțiune pentru fiecare element.

Exemplu din UserService.removeUser():

List<Friendship> friendshipsToDelete =
        friendshipRepository.findAll().stream()
                .filter(friendship ->
                        friendship.getUserId1().equals(id)
                        || friendship.getUserId2().equals(id))
                .collect(Collectors.toList());

În acest exemplu, `stream()` creează fluxul, `filter()` este operație intermediară, iar `collect()` este operație terminală.

## 4. Interfețe funcționale utilizate

Interfață funcțională                   Unde este utilizată                           Rol
1.Validator<T> Validator.java           Main.java                     Validarea entităților și compunerea                                                                       regulilor cu andThen()
2.Predicate<T> Repository.java          CriteriiUtilizator.java       Definirea și combinarea criteriilor                                                                       de căutare
3.Function<T,R>                         RepositoryFactory.java        Crearea repository-urilor și                                                                              transformarea valorilor
4.Consumer<T>                           ConsoleUI.java operații       Executarea unei actiuni pentru                                              forEach()                     fiecare element
5.Supplier<T>                           ArrayList::new în InMemoryRepository  Crearea unui obiect la cerere

6.Comparator<T>                         UserService.java CommunityService.java Sortarea utilizatorilor și                                                                                compararea comunităților

7.BinaryOperator<T>                     Integer::sum în Lab4Demo.java           Combinarea valorilor  prin
                                                                                reduce()

Am folosit cele patru tipuri de referințe la metode:
- Metodă statică: CriteriiUtilizator::faraPrieteni
- Metodă de instanță a unui obiect: System.out::println
- Metodă de instanță a unui obiect oarecare: User::getLastName
- Constructor: InMemoryRepository::new, ArrayList::new

## 5. Alegerea pentru Optional în Repository
Am ales să păstrez Optional<E> ca tip de retur pentru metoda findOne(ID id) din interfața Repository.
Motivul este că o căutare poate să nu găsească entitatea solicitată.
Optional permite tratarea explicită a acestui caz, fără utilizarea valorii null.

## 6. Alegerea pentru Template Method vs. Function
Am ales să păstrez design pattern-ul **Template Method** pentru persistența în fișiere.
Clasa AbstractFileRepository implementează algoritmul general de citire și scriere a fișierelor.
Clasele UtilizatorFileRepository și PrietenieFileRepository implementează metodele specifice de conversie:
- extractEntity(String line) – transformă textul în entitate.
- createEntityAsString(E entity) – transformă entitatea în text.

## 7. Bucle păstrate și justificare

În ConsoleUI.run() am păstrat bucla:

while (running) {
    // Menu operations
}

Această buclă controlează repetarea meniului până când utilizatorul alege ieșirea. Ea nu este folosită pentru parcurgerea unei colecții.
