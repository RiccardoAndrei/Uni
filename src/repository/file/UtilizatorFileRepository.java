package repository.file;

import domain.User;
import exceptions.RepositoryException;
import validation.Validator;

import java.nio.file.Path;

/**
 * Text file repository for users, using lines id;firstName;lastName.
 */
public class UtilizatorFileRepository
        extends AbstractFileRepository<Long, User> {

    /**
     * Constructs and loads the user repository.
     *
     * @param filePath user text file path
     * @param validator user validation strategy
     * @throws RepositoryException if the file cannot be read
     */
    public UtilizatorFileRepository(
            Path filePath, Validator<User> validator) {

        super(filePath, validator);
        loadFromFile();
    }

    /** {@inheritDoc} */
    @Override
    protected User extractEntity(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != 3) {
            throw new RepositoryException(
                    "Expected three fields: id;firstName;lastName."
            );
        }

        try {
            Long id = Long.valueOf(fields[0].trim());

            return new User(
                    id,
                    fields[1].trim(),
                    fields[2].trim()
            );

        } catch (NumberFormatException e) {
            throw new RepositoryException(
                    "Invalid numeric user ID: '" + fields[0] + "'.", e
            );
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String createEntityAsString(User entity) {
        return entity.getId() + ";"
                + entity.getFirstName() + ";"
                + entity.getLastName();
    }
}
