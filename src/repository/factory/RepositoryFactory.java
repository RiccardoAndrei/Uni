package repository.factory;

import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.RepositoryException;
import repository.InMemoryRepository;
import repository.Repository;
import repository.RepositoryType;
import repository.db.PrietenieDbRepository;
import repository.db.UtilizatorDbRepository;
import repository.file.PrietenieFileRepository;
import repository.file.UtilizatorFileRepository;
import validation.FriendshipWithUsersValidator;
import validation.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.nio.file.Files;

/**
 * Simple Factory that creates concrete repositories according to configuration.
 * All repositories use the same persistence type.
 */
public class RepositoryFactory {

    private final Properties properties;
    private final RepositoryType type;

    private Repository<Long, User> userRepository;
    private Repository<Tuple<Long, Long>, Friendship> friendshipRepository;

    /**
     * Loads and validates the application configuration.
     *
     * @throws RepositoryException if configuration is missing or invalid
     */
    public RepositoryFactory() {
        properties = new Properties();

        try (InputStream input = Files.newInputStream(
                Path.of("src/main/resources/config.properties"))) {
            if (input == null) {
                throw new RepositoryException(
                        "Missing src/main/resources/config.properties on the classpath."
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot read config.properties.", e
            );
        }

        String rawType = required("repository.type");

        try {
            type = RepositoryType.valueOf(
                    rawType.toUpperCase(java.util.Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {
            throw new RepositoryException(
                    "Unknown repository.type: '" + rawType
                            + "'. Expected MEMORY, FILE or DATABASE.", e
            );
        }

        required("users.file");
        required("friendships.file");
        required("db.url");
        required("db.user");
        required("db.password");
    }

    /**
     * Returns the selected persistence type.
     *
     * @return configured repository type
     */
    public RepositoryType getType() {
        return type;
    }

    private String required(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new RepositoryException(
                    "Missing or empty configuration key: " + key
            );
        }

        return value.trim();
    }

    /**
     * Creates the user repository according to the configured type.
     *
     * @param validator user validation strategy
     * @return abstract user repository
     * @throws RepositoryException if initialization fails
     */
    public Repository<Long, User> createUtilizatorRepository(
            Validator<User> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "User validator must not be null."
            );
        }

        if (userRepository != null) {
            return userRepository;
        }

        switch (type) {
            case MEMORY:
                userRepository =
                        new InMemoryRepository<Long, User>(validator);
                break;

            case FILE:
                userRepository = new UtilizatorFileRepository(
                        Path.of(required("users.file")), validator
                );
                break;

            case DATABASE:
                userRepository = new UtilizatorDbRepository(
                        validator,
                        required("db.url"),
                        required("db.user"),
                        required("db.password")
                );
                break;

            default:
                throw new RepositoryException(
                        "Unsupported repository type: " + type
                );
        }

        return userRepository;
    }

    /**
     * Creates the friendship repository and reconstructs friend lists.
     *
     * @param validator friendship validation strategy
     * @return abstract friendship repository
     * @throws IllegalStateException if user repository is not initialized
     * @throws RepositoryException if initialization fails
     */
    public Repository<Tuple<Long, Long>, Friendship>
    createPrietenieRepository(Validator<Friendship> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "Friendship validator must not be null."
            );
        }

        if (userRepository == null) {
            throw new IllegalStateException(
                    "Create the user repository first."
            );
        }

        if (friendshipRepository != null) {
            return friendshipRepository;
        }

        Validator<Friendship> validatedWithUsers =
                new FriendshipWithUsersValidator(
                        validator, userRepository
                );

        switch (type) {
            case MEMORY:
                friendshipRepository =
                        new InMemoryRepository<Tuple<Long, Long>, Friendship>(
                                validatedWithUsers
                        );
                break;

            case FILE:
                friendshipRepository = new PrietenieFileRepository(
                        Path.of(required("friendships.file")),
                        validatedWithUsers
                );
                break;

            case DATABASE:
                friendshipRepository = new PrietenieDbRepository(
                        validatedWithUsers,
                        required("db.url"),
                        required("db.user"),
                        required("db.password")
                );
                break;

            default:
                throw new RepositoryException(
                        "Unsupported repository type: " + type
                );
        }

        if (type == RepositoryType.FILE) {
            rebuildFriendLists();
        }

        return friendshipRepository;
    }

    private void rebuildFriendLists() {
        List<Friendship> friendships = friendshipRepository.findAll();

        for (Friendship friendship : friendships) {
            Optional<User> first =
                    userRepository.findOne(friendship.getUserId1());

            Optional<User> second =
                    userRepository.findOne(friendship.getUserId2());

            if (first.isPresent() && second.isPresent()) {
                first.get().addFriend(friendship.getUserId2());
                second.get().addFriend(friendship.getUserId1());
            }
        }
    }
}
