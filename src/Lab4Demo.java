import criteria.CriteriiUtilizator;
import domain.User;
import repository.Repository;
import repository.factory.RepositoryFactory;
import validation.UserValidator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Demonstrates Java 8 functional programming features.
 */
public class Lab4Demo {

    private Lab4Demo() {
    }

    /**
     * Demonstrates predicates, method references and stream operations.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {

        RepositoryFactory factory = new RepositoryFactory();

        Repository<Long, User> users =
                factory.createUtilizatorRepository(
                        new UserValidator()
                );

        Predicate<User> criterion =
                CriteriiUtilizator.cuNumele("Popescu")
                        .or(CriteriiUtilizator.prenumeIncepeCu("A"))
                        .and(Predicate.not(
                                CriteriiUtilizator::faraPrieteni
                        ));

        System.out.println("Filtered users:");

        users.findAll(criterion)
                .forEach(System.out::println);

        Predicate<User> otherNames =
                CriteriiUtilizator.cuNumele("Popescu")
                        .negate();

        System.out.println(
                "Users with other last names: "
                        + users.findAll(otherNames).size()
        );

        List<User> allUsers = users.findAll();

        long totalUsers = allUsers.stream().count();

        boolean anyWithoutFriends =
                allUsers.stream()
                        .anyMatch(CriteriiUtilizator::faraPrieteni);

        boolean allHavePositiveIds =
                allUsers.stream()
                        .allMatch(user -> user.getId() > 0);

        boolean noneHaveNegativeIds =
                allUsers.stream()
                        .noneMatch(user -> user.getId() < 0);

        System.out.println("Total users: " + totalUsers);
        System.out.println(
                "Any users without friends: " + anyWithoutFriends
        );
        System.out.println(
                "All users have positive IDs: " + allHavePositiveIds
        );
        System.out.println(
                "No users have negative IDs: " + noneHaveNegativeIds
        );

        int totalFriendReferences =
                allUsers.stream()
                        .map(user -> user.getFriends().size())
                        .reduce(0, Integer::sum);

        System.out.println(
                "Total friend references: " + totalFriendReferences
        );

        List<User> sortedUsers =
                allUsers.stream()
                        .sorted(
                                Comparator.comparing(User::getLastName)
                                        .thenComparing(User::getFirstName)
                        )
                        .collect(Collectors.toList());

        System.out.println("Sorted users:");
        sortedUsers.forEach(System.out::println);

        Map<String, Long> usersByLastName =
                allUsers.stream()
                        .collect(
                                Collectors.groupingBy(
                                        User::getLastName,
                                        Collectors.counting()
                                )
                        );

        System.out.println(
                "Users grouped by last name: " + usersByLastName
        );

        String names =
                allUsers.stream()
                        .map(User::getFirstName)
                        .sorted()
                        .collect(Collectors.joining(", "));

        System.out.println("First names: " + names);
    }
}