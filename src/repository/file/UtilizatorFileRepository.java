
package repository.file;

import domain.User;
import exceptions.RepositoryException;
import validation.Validator;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Text file repository for users.
 * Supports the original three-field format and the
 * extended six-field format.
 */
public class UtilizatorFileRepository
        extends AbstractFileRepository<Long, User> {

    /**
     * Constructs and loads the user repository.
     *
     * @param filePath user text file path
     * @param validator user validation strategy
     * @throws RepositoryException if reading the file fails
     */
    public UtilizatorFileRepository(
            Path filePath, Validator<User> validator) {

        super(filePath, validator);
        loadFromFile();
    }

    /**
     * Converts a text record into a user.
     *
     * @param line text record
     * @return parsed user
     * @throws RepositoryException if the record is invalid
     */
    @Override
    protected User extractEntity(String line) {

        String[] fields = line.split(";", -1);

        if (fields.length != 3 && fields.length != 6) {
            throw new RepositoryException(
                    "Expected 3 or 6 user fields."
            );
        }

        Long id;

        try {
            id = Long.valueOf(fields[0].trim());

        } catch (NumberFormatException e) {
            throw new RepositoryException(
                    "Invalid user ID: " + fields[0], e
            );
        }

        String firstName = fields[1].trim();
        String lastName = fields[2].trim();

        if (fields.length == 3) {
            return new User(id, firstName, lastName);
        }

        String city = fields[3].trim();
        String email = fields[4].trim();

        LocalDate birthDate = null;

        if (!fields[5].trim().isEmpty()) {
            try {
                birthDate = LocalDate.parse(fields[5].trim());

            } catch (DateTimeParseException e) {
                throw new RepositoryException(
                        "Invalid birth date: " + fields[5], e
                );
            }
        }

        return new User(
                id,
                firstName,
                lastName,
                city,
                email,
                birthDate
        );
    }

    /**
     * Converts a user into a semicolon-separated text record.
     *
     * @param user user to serialize
     * @return formatted user record
     */
    @Override
    protected String createEntityAsString(User user) {

        String city = user.getCity() == null
                ? "" : user.getCity();

        String email = user.getEmail() == null
                ? "" : user.getEmail();

        String birthDate = user.getBirthDate() == null
                ? "" : user.getBirthDate().toString();

        return user.getId() + ";"
                + user.getFirstName() + ";"
                + user.getLastName() + ";"
                + city + ";"
                + email + ";"
                + birthDate;
    }
}
