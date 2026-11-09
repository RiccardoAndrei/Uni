package criteria;

import domain.User;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * Reusable predicates for searching social-network users.
 */
public final class CriteriiUtilizator {

    private CriteriiUtilizator() {
    }

    /**
     * Checks whether a user has no friends.
     *
     * @param user the user to check
     * @return true if the user has no friends
     */
    public static boolean faraPrieteni(User user) {

        return user.getFriends().isEmpty();
    }

    /**
     * Creates a predicate for a specified last name.
     *
     * @param lastName the required last name
     * @return a predicate checking the last name
     * @throws IllegalArgumentException if lastName is null
     */
    public static Predicate<User> cuNumele(String lastName) {
        if (lastName == null) {
            throw new IllegalArgumentException(
                    "Last name must not be null."
            );
        }

        return user -> user.getLastName()
                .equalsIgnoreCase(lastName);
    }

    /**
     * Creates a predicate for a first-name prefix.
     *
     * @param prefix the required prefix
     * @return a predicate checking the prefix
     * @throws IllegalArgumentException if prefix is null
     */
    public static Predicate<User> prenumeIncepeCu(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException(
                    "Prefix must not be null."
            );
        }

        String normalized = prefix.toLowerCase(Locale.ROOT);

        return user -> user.getFirstName()
                .toLowerCase(Locale.ROOT)
                .startsWith(normalized);
    }

    /**
     * Creates a predicate for a minimum number of friends.
     *
     * @param minimum the minimum required number of friends
     * @return a predicate checking the friend count
     */
    public static Predicate<User> minimPrieteni(int minimum) {

        return user -> user.getFriends().size() >= minimum;
    }
}
