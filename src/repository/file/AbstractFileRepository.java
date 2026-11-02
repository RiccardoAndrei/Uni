package repository.file;

import domain.Entity;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import repository.InMemoryRepository;
import validation.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

/**
 * Template Method repository that loads a UTF-8 text file and persists after
 * each successful save, delete, or update. Concrete subclasses implement only
 * conversion between a text line and an entity.
 *
 * @param <ID> entity identifier type
 * @param <E> entity type
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>>
        extends InMemoryRepository<ID, E> {

    private final Path filePath;

    /**
     * Configures a file-backed repository. Subclasses must invoke
     * loadFromFile() at the end of their constructors, once fully initialized.
     *
     * @param filePath path of the UTF-8 text file
     * @param validator entity validation strategy
     * @throws IllegalArgumentException if an argument is null
     */
    protected AbstractFileRepository(Path filePath, Validator<E> validator) {
        super(validator);

        if (filePath == null || validator == null) {
            throw new IllegalArgumentException(
                    "File path and validator must not be null."
            );
        }

        this.filePath = filePath;
    }

    /**
     * Converts a text line into an entity.
     *
     * @param line source text line
     * @return converted entity
     * @throws RepositoryException if the record cannot be parsed
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity to a text line.
     *
     * @param entity domain entity
     * @return line to persist
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Loads records only into memory, without rewriting the file.
     * An absent file leaves the repository empty; malformed rows are reported
     * with their line numbers and skipped.
     *
     * @throws RepositoryException if an existing file cannot be read
     */
    protected final void loadFromFile() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                filePath, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {
                    E entity = extractEntity(line);

                    // Use super.save to avoid rewriting while reading.
                    // This also validates the loaded entity.
                    super.save(entity);

                } catch (RepositoryException e) {
                    reportBadLine(lineNumber, e.getMessage());

                } catch (ValidationException e) {
                    reportBadLine(lineNumber, e.getMessage());

                } catch (IllegalArgumentException e) {
                    reportBadLine(lineNumber, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot read file '" + filePath + "'.", e
            );
        }
    }

    private void reportBadLine(int lineNumber, String reason) {
        System.err.println(
                "Ignoring invalid line " + lineNumber + " in '"
                        + filePath + "': " + reason
        );
    }

    /** {@inheritDoc} */
    @Override
    public void save(E entity) {
        checkEntity(entity);
        super.save(entity);

        try {
            writeToFile();
        } catch (RepositoryException e) {
            super.delete(entity.getId());
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(ID id) {
        checkId(id);

        Optional<E> previous = super.findOne(id);

        if (previous.isEmpty()) {
            return false;
        }

        super.delete(id);

        try {
            writeToFile();
            return true;

        } catch (RepositoryException e) {
            super.save(previous.get());
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void update(E entity) {
        checkEntity(entity);
        Optional<E> previous = super.findOne(entity.getId());

        super.update(entity);

        try {
            writeToFile();

        } catch (RepositoryException e) {
            if (previous.isPresent()) {
                super.update(previous.get());
            }

            throw e;
        }
    }

    /**
     * Rewrites the whole text file in UTF-8 using a temporary file, then
     * replaces the destination. File streams are always closed automatically.
     *
     * @throws RepositoryException if writing or moving the file fails
     */
    private void writeToFile() {
        Path parent = filePath.toAbsolutePath().getParent();
        Path temporary = null;

        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(
                    parent, "repository-", ".tmp"
            );

            try (BufferedWriter writer = Files.newBufferedWriter(
                    temporary,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING)) {

                List<E> entities = super.findAll();

                for (E entity : entities) {
                    writer.write(createEntityAsString(entity));
                    writer.newLine();
                }
            }

            try {
                Files.move(
                        temporary,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporary,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot write file '" + filePath + "'.", e
            );

        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);

                } catch (IOException cleanupError) {
                    System.err.println(
                            "Could not remove temporary file: "
                                    + cleanupError.getMessage()
                    );
                }
            }
        }
    }
}
