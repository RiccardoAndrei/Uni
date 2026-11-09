package repository.factory;

import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import repository.InMemoryRepository;
import repository.Repository;
import repository.RepositoryType;
import repository.db.PrietenieDbRepository;
import repository.db.UtilizatorDbRepository;
import repository.file.PrietenieFileRepository;
import repository.file.UtilizatorFileRepository;
import validation.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Creates repositories according to the configured persistence type.
 * Uses a map of factory functions instead of a switch.
 */
public class RepositoryFactory {

    private final Properties properties = new Properties();
    private final RepositoryType type;

    private Repository<Long, User> userRepository;

    private Repository<Tuple<Long, Long>, Friendship>
            friendshipRepository;

    /**
     * Loads repository configuration from config.properties.
     *
     * @throws RepositoryException if configuration is missing or invalid
     */
    public RepositoryFactory() {
        Path configPath = Path.of(
                "src/main/resources/config.properties"
        );

        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot read configuration: " + configPath, e
            );
        }

        String rawType = required("repository.type");

        try {
            type = RepositoryType.valueOf(
                    rawType.toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {
            throw new RepositoryException(
                    "Invalid repository.type: " + rawType, e
            );
        }

        required("users.file");
        required("friendships.file");
        required("db.url");
        required("db.user");
        required("db.password");
    }

    private String required(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new RepositoryException(
                    "Missing configuration key: " + key
            );
        }

        return value.trim();
    }

    /**
     * Returns the selected repository type.
     *
     * @return the repository type
     */
    public RepositoryType getType() {
        return type;
    }

    /**
     * Creates the configured user repository.
     *
     * @param validator the user validator
     * @return the user repository
     * @throws IllegalArgumentException if validator is null
     */
    public Repository<Long, User> createUtilizatorRepository(
            Validator<User> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "Validator must not be null."
            );
        }

        if (userRepository != null) {
            return userRepository;
        }

        Map<RepositoryType,
                Function<Validator<User>, Repository<Long, User>>>
                creators = new EnumMap<>(RepositoryType.class);

        creators.put(
                RepositoryType.MEMORY,
                InMemoryRepository::new
        );

        creators.put(
                RepositoryType.FILE,
                v -> new UtilizatorFileRepository(
                        Path.of(required("users.file")),
                        v
                )
        );

        creators.put(
                RepositoryType.DATABASE,
                v -> new UtilizatorDbRepository(
                        v,
                        required("db.url"),
                        required("db.user"),
                        required("db.password")
                )
        );

        userRepository = creators.get(type).apply(validator);

        return userRepository;
    }

    /**
     * Creates the configured friendship repository.
     * Adds a lambda rule requiring both users to exist.
     *
     * @param validator the friendship validator
     * @return the friendship repository
     * @throws IllegalArgumentException if validator is null
     * @throws IllegalStateException if user repository is not initialized
     */
    public Repository<Tuple<Long, Long>, Friendship>
    createPrietenieRepository(Validator<Friendship> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "Validator must not be null."
            );
        }

        if (userRepository == null) {
            throw new IllegalStateException(
                    "Create user repository first."
            );
        }

        if (friendshipRepository != null) {
            return friendshipRepository;
        }

        Validator<Friendship> validatorWithUsers =
                validator.andThen(friendship -> {

                    boolean firstExists = userRepository
                            .findOne(friendship.getUserId1())
                            .isPresent();

                    boolean secondExists = userRepository
                            .findOne(friendship.getUserId2())
                            .isPresent();

                    if (!firstExists || !secondExists) {
                        throw new ValidationException(
                                "Friendship contains a nonexistent user."
                        );
                    }
                });

        Map<RepositoryType,
                Function<Validator<Friendship>,
                        Repository<Tuple<Long, Long>, Friendship>>>
                creators = new EnumMap<>(RepositoryType.class);

        creators.put(
                RepositoryType.MEMORY,
                InMemoryRepository::new
        );

        creators.put(
                RepositoryType.FILE,
                v -> new PrietenieFileRepository(
                        Path.of(required("friendships.file")),
                        v
                )
        );

        creators.put(
                RepositoryType.DATABASE,
                v -> new PrietenieDbRepository(
                        v,
                        required("db.url"),
                        required("db.user"),
                        required("db.password")
                )
        );

        friendshipRepository =
                creators.get(type).apply(validatorWithUsers);

        if (type == RepositoryType.FILE) {
            rebuildFriendLists();
        }

        return friendshipRepository;
    }

    private void rebuildFriendLists() {
        friendshipRepository.findAll().forEach(friendship -> {

            userRepository.findOne(friendship.getUserId1())
                    .ifPresent(user ->
                            user.addFriend(friendship.getUserId2()));

            userRepository.findOne(friendship.getUserId2())
                    .ifPresent(user ->
                            user.addFriend(friendship.getUserId1()));
        });
    }
}
