package validation;

import domain.User;
import exceptions.ValidationException;

import java.time.LocalDate;

/**
 * Validates users for memory, file and database storage.
 */
public class UserValidator implements Validator<User> {

    private final boolean databaseMode;

    /**
     * Creates a validator for manually assigned IDs.
     */
    public UserValidator() {
        this(false);
    }

    /**
     * Creates a validator for the selected storage mode.
     *
     * @param databaseMode true when PostgreSQL generates IDs
     */
    public UserValidator(boolean databaseMode) {
        this.databaseMode = databaseMode;
    }

    /** {@inheritDoc} */
    @Override
    public void validate(User user) {

        if (user == null) {
            throw new ValidationException(
                    "User must not be null."
            );
        }

        StringBuilder errors = new StringBuilder();

        if ((user.getId() == null && !databaseMode)
                || (user.getId() != null
                && user.getId() <= 0)) {

            errors.append("Invalid user ID.\n");
        }

        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        if (firstName == null || firstName.isBlank()) {
            errors.append("First name is required.\n");

        } else {
            if (!Character.isUpperCase(firstName.charAt(0))) {
                errors.append(
                        "First name must start with uppercase.\n"
                );
            }

            if (!databaseMode && invalidFileText(firstName)) {
                errors.append(
                        "Invalid characters in first name.\n"
                );
            }
        }

        if (lastName == null || lastName.isBlank()) {
            errors.append("Last name is required.\n");

        } else {
            if (!Character.isUpperCase(lastName.charAt(0))) {
                errors.append(
                        "Last name must start with uppercase.\n"
                );
            }

            if (!databaseMode && invalidFileText(lastName)) {
                errors.append(
                        "Invalid characters in last name.\n"
                );
            }
        }

        String city = user.getCity();
        String email = user.getEmail();
        LocalDate birthDate = user.getBirthDate();

        boolean hasCity =
                city != null && !city.isBlank();

        boolean hasEmail =
                email != null && !email.isBlank();

        // Old users may have no extended profile fields.
        if (hasCity || hasEmail || birthDate != null) {

            if (!hasCity) {
                errors.append("City is required.\n");

            } else if (!databaseMode && invalidFileText(city)) {
                errors.append("Invalid city.\n");
            }

            if (!hasEmail || !email.matches(
                    "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
            )) {
                errors.append("Invalid email.\n");
            }

            if (birthDate == null
                    || birthDate.isAfter(LocalDate.now())) {

                errors.append("Invalid birth date.\n");
            }
        }

        if (errors.length() > 0) {
            throw new ValidationException(
                    errors.toString()
            );
        }
    }

    private boolean invalidFileText(String text) {
        return text.contains(";")
                || text.contains("\r")
                || text.contains("\n");
    }
}


