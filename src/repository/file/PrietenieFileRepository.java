package repository.file;

import domain.Friendship;
import domain.Tuple;
import exceptions.RepositoryException;
import validation.Validator;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Text file repository for friendships using id1;id2;ISO-local-date-time.
 */
public class PrietenieFileRepository
        extends AbstractFileRepository<Tuple<Long, Long>, Friendship> {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Constructs and loads the friendship repository.
     *
     * @param filePath friendship file path
     * @param validator friendship validation strategy
     * @throws RepositoryException if the file cannot be read
     */
    public PrietenieFileRepository(
            Path filePath, Validator<Friendship> validator) {

        super(filePath, validator);
        loadFromFile();
    }

    /** {@inheritDoc} */
    @Override
    protected Friendship extractEntity(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != 3) {
            throw new RepositoryException(
                    "Expected three fields: userId1;userId2;date."
            );
        }

        Long id1;
        Long id2;

        try {
            id1 = Long.valueOf(fields[0].trim());
            id2 = Long.valueOf(fields[1].trim());

        } catch (NumberFormatException e) {
            throw new RepositoryException(
                    "Invalid friendship user ID.", e
            );
        }

        LocalDateTime date;

        try {
            date = LocalDateTime.parse(fields[2].trim(), FORMAT);

        } catch (DateTimeParseException e) {
            throw new RepositoryException(
                    "Invalid ISO friendship date: '" + fields[2] + "'.", e
            );
        }

        return new Friendship(id1, id2, date);
    }

    /** {@inheritDoc} */
    @Override
    protected String createEntityAsString(Friendship entity) {
        return entity.getUserId1() + ";"
                + entity.getUserId2() + ";"
                + entity.getFriendshipDate().format(FORMAT);
    }
}
