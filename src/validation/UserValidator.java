package validation;

import domain.User;
import exceptions.ValidationException;

/**
 * Validates a user and reports all validation errors
 * in a single ValidationException.
 */
public class UserValidator implements Validator<User> {

    /**
     * Validates the user's identifier, first name and last name.
     *
     * @param user the user to validate
     * @throws ValidationException if the user is invalid
     */
    @Override
    public void validate(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null.");
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
                errors.append("First name must start with an uppercase letter.\n");
            }
            if (firstName.contains(";") ||
                    firstName.contains("\n") ||
                    firstName.contains("\r")) {
                errors.append("First name contains invalid characters.\n");
            }
        }

        if (lastName == null || lastName.isBlank()) {
            errors.append("Last name cannot be empty.\n");
        } else {
            if (!Character.isUpperCase(lastName.charAt(0))) {
                errors.append("Last name must start with an uppercase letter.\n");
            }
            if (lastName.contains(";") ||
                    lastName.contains("\n") ||
                    lastName.contains("\r")) {
                errors.append("Last name contains invalid characters.\n");
            }
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}

