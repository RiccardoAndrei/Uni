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
 * Template Method for file-based persistence.
 *
 * @param <ID> identifier type
 * @param <E> entity type
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>>
        extends InMemoryRepository<ID, E> {

    private final Path filePath;

    /**
     * Creates a file repository.
     *
     * @param filePath file location
     * @param validator entity validator
     */
    protected AbstractFileRepository(
            Path filePath, Validator<E> validator) {

        super(validator);

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path must not be null."
            );
        }

        this.filePath = filePath;
    }

    /**
     * Converts text into an entity.
     *
     * @param line file line
     * @return parsed entity
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into text.
     *
     * @param entity entity to serialize
     * @return text representation
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Loads valid records from the file.
     */
    protected final void loadFromFile() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (Stream<String> lines = Files.lines(
                filePath, StandardCharsets.UTF_8)) {

            List<String> records =
                    lines.collect(Collectors.toList());

            IntStream.range(0, records.size())
                    .filter(i -> !records.get(i).isBlank())
                    .forEach(i -> {
                        try {
                            super.save(
                                    extractEntity(records.get(i))
                            );

                        } catch (ValidationException
                                 | RepositoryException
                                 | IllegalArgumentException e) {

                            System.err.println(
                                    "Invalid line " + (i + 1)
                                            + " in " + filePath
                                            + ": " + e.getMessage()
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
    public E delete(ID id) {
        checkId(id);

        Optional<E> previous = super.findOne(id);

        if (previous.isEmpty()) {
            return null;
        }

        super.delete(id);

        try {
            writeToFile();
            return previous.orElseThrow();

        } catch (RepositoryException e) {
            super.save(previous.orElseThrow());
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public E update(E entity) {
        checkEntity(entity);

        Optional<E> previous =
                super.findOne(entity.getId());

        E notFound = super.update(entity);

        if (notFound != null) {
            return notFound;
        }

        try {
            writeToFile();

        } catch (RepositoryException e) {
            previous.ifPresent(super::update);
            throw e;
        }

        return null;
    }

    /**
     * Rewrites the UTF-8 file using a temporary file.
     */
    private void writeToFile() {
        Path parent = filePath.toAbsolutePath().getParent();
        Path temporary = null;

        try {
            Files.createDirectories(parent);

            temporary = Files.createTempFile(
                    parent, "repository-", ".tmp"
            );

            List<String> records = super.findAll().stream()
                    .map(this::createEntityAsString)
                    .collect(Collectors.toList());

            Files.write(
                    temporary,
                    records,
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
                            "Temporary file cleanup failed: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }
}
