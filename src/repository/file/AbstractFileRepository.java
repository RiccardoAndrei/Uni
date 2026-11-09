package repository.file;

import domain.Entity;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import repository.InMemoryRepository;
import validation.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * File repository using Template Method and Stream API.
 * Data is stored in UTF-8 text files.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>>
        extends InMemoryRepository<ID, E> {

    private final Path filePath;

    /**
     * Initializes the file repository.
     *
     * @param filePath the file path
     * @param validator the entity validator
     * @throws IllegalArgumentException if filePath is null
     */
    protected AbstractFileRepository(
            Path filePath,
            Validator<E> validator) {

        super(validator);

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path must not be null."
            );
        }

        this.filePath = filePath;
    }

    /**
     * Converts a text line into an entity.
     *
     * @param line the text line
     * @return the converted entity
     * @throws RepositoryException if parsing fails
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into a text line.
     *
     * @param entity the entity
     * @return the formatted text
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Loads data from the file using Stream API.
     * Invalid lines are reported and ignored.
     *
     * @throws RepositoryException if the file cannot be read
     */
    protected final void loadFromFile() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (Stream<String> lines = Files.lines(
                filePath, StandardCharsets.UTF_8)) {

            List<String> records = lines.collect(Collectors.toList());

            IntStream.range(0, records.size())
                    .filter(index -> !records.get(index).isBlank())
                    .forEach(index -> {
                        try {
                            E entity = extractEntity(records.get(index));

                            // Avoid rewriting the file while loading.
                            super.save(entity);

                        } catch (ValidationException
                                 | RepositoryException
                                 | IllegalArgumentException e) {

                            System.err.println(
                                    "Invalid line " + (index + 1)
                                            + " in '" + filePath
                                            + "': " + e.getMessage()
                            );
                        }
                    });

        } catch (IOException | UncheckedIOException e) {
            throw new RepositoryException(
                    "Cannot read file: " + filePath, e
            );
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save(E entity) {
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
            super.save(previous.orElseThrow());
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
            previous.ifPresent(super::update);
            throw e;
        }
    }

    /**
     * Rewrites the text file using a temporary file.
     *
     * @throws RepositoryException if writing fails
     */
    private void writeToFile() {
        Path parent = filePath.toAbsolutePath().getParent();
        Path temporary = null;

        try {
            Files.createDirectories(parent);

            temporary = Files.createTempFile(
                    parent, "repository-", ".tmp"
            );

            List<String> lines = super.findAll().stream()
                    .map(this::createEntityAsString)
                    .collect(Collectors.toList());

            Files.write(
                    temporary,
                    lines,
                    StandardCharsets.UTF_8
            );

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
                    "Cannot write file: " + filePath, e
            );

        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);

                } catch (IOException e) {
                    System.err.println(
                            "Could not delete temporary file: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }
}
