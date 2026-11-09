package validation;

import domain.User;
import exceptions.ValidationException;

import java.time.LocalDate;

/**
 * Validates a user and reports all validation errors
 * in a single ValidationException.
 */
public class UserValidator implements Validator<User> {

    /**
     * Validates the identifier, names and profile information.
     * Legacy users without profile information are also accepted.
     *
     * @param user user to validate
     * @throws ValidationException if the user contains invalid data
     */
    @Override
    public void validate(User user) {

        if (user == null) {
            throw new ValidationException(
                    "User cannot be null."
            );
        }

        StringBuilder errors = new StringBuilder();

        if (user.getId() == null || user.getId() <= 0) {
            errors.append("User ID must be positive.\n");
        }

        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        if (firstName == null || firstName.isBlank()) {
            errors.append("First name cannot be empty.\n");
        } else {
            if (!Character.isUpperCase(firstName.charAt(0))) {
                errors.append(
                        "First name must start with an uppercase letter.\n"
                );
            }

            if (containsInvalidCharacters(firstName)) {
                errors.append(
                        "First name contains invalid characters.\n"
                );
            }
        }

        if (lastName == null || lastName.isBlank()) {
            errors.append("Last name cannot be empty.\n");
        } else {
            if (!Character.isUpperCase(lastName.charAt(0))) {
                errors.append(
                        "Last name must start with an uppercase letter.\n"
                );
            }

            if (containsInvalidCharacters(lastName)) {
                errors.append(
                        "Last name contains invalid characters.\n"
                );
            }
        }

        String city = user.getCity();
        String email = user.getEmail();
        LocalDate birthDate = user.getBirthDate();

        boolean hasCity = city != null && !city.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasBirthDate = birthDate != null;

        boolean legacyProfile =
                !hasCity && !hasEmail && !hasBirthDate;

        if (!legacyProfile) {

            if (!hasCity) {
                errors.append("City cannot be empty.\n");
            } else if (containsInvalidCharacters(city)) {
                errors.append(
                        "City contains invalid characters.\n"
                );
            }

            if (!hasEmail) {
                errors.append("Email cannot be empty.\n");

            } else if (!email.matches(
                    "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
            )) {
                errors.append("Invalid email address.\n");
            }

            if (!hasBirthDate) {
                errors.append("Birth date cannot be empty.\n");

            } else if (birthDate.isAfter(LocalDate.now())) {
                errors.append(
                        "Birth date cannot be in the future.\n"
                );
            }
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }

    private boolean containsInvalidCharacters(String value) {
        return value.contains(";")
                || value.contains("\n")
                || value.contains("\r");
    }
}


