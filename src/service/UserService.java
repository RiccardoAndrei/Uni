package service;

import criteria.CriteriiUtilizator;
import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.ServiceException;
import repository.Repository;
import validation.Validator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides user operations using repository abstractions.
 */
public class UserService {

    private final Repository<Long, User> userRepository;

    private final Repository<Tuple<Long, Long>, Friendship>
            friendshipRepository;

    private final Validator<User> validator;

    /**
     * Creates the user service.
     *
     * @param userRepository user repository
     * @param friendshipRepository friendship repository
     * @param validator user validator
     */
    public UserService(
            Repository<Long, User> userRepository,
            Repository<Tuple<Long, Long>, Friendship> friendshipRepository,
            Validator<User> validator) {

        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.validator = validator;
    }

    /**
     * Adds a user with the original fields.
     *
     * @param id user ID
     * @param firstName first name
     * @param lastName last name
     * @return saved user
     */
    public User addUser(
            Long id,
            String firstName,
            String lastName) {

        return addUser(
                id, firstName, lastName,
                "", "", null
        );
    }

    /**
     * Adds a user with extended profile information.
     *
     * @param id ID or null for a database-generated ID
     * @param firstName first name
     * @param lastName last name
     * @param city city of residence
     * @param email email address
     * @param birthDate birth date
     * @return saved user with its identifier
     */
    public User addUser(
            Long id,
            String firstName,
            String lastName,
            String city,
            String email,
            LocalDate birthDate) {

        User user = new User(
                id,
                firstName,
                lastName,
                city,
                email,
                birthDate
        );

        validator.validate(user);
        userRepository.save(user);

        return user;
    }

    /**
     * Removes a user and their friendships.
     *
     * @param id user ID
     * @throws ServiceException if user does not exist
     */
    public void removeUser(Long id) {

        if (id == null) {
            throw new ServiceException(
                    "User does not exist."
            );
        }

        userRepository.findOne(id)
                .orElseThrow(() ->
                        new ServiceException(
                                "User does not exist."
                        ));

        List<Friendship> friendshipsToDelete =
                friendshipRepository.findAll().stream()
                        .filter(friendship ->
                                friendship.getUserId1().equals(id)
                                        || friendship.getUserId2().equals(id))
                        .collect(Collectors.toList());

        friendshipsToDelete.forEach(friendship -> {

            Long otherId =
                    friendship.getUserId1().equals(id)
                            ? friendship.getUserId2()
                            : friendship.getUserId1();

            friendshipRepository.delete(friendship.getId());

            userRepository.findOne(otherId)
                    .ifPresent(user ->
                            user.removeFriend(id));
        });

        userRepository.delete(id);
    }

    /**
     * Returns all users.
     *
     * @return user list
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds users matching a predicate.
     *
     * @param criterion search predicate
     * @return sorted matching users
     */
    public List<User> findUsers(Predicate<User> criterion) {

        return userRepository.findAll(criterion).stream()
                .sorted(
                        Comparator.comparing(User::getLastName)
                                .thenComparing(User::getFirstName)
                )
                .collect(Collectors.toList());
    }

    /**
     * Returns users without friends.
     *
     * @return users without friends
     */
    public List<User> getUsersWithoutFriends() {
        return findUsers(CriteriiUtilizator::faraPrieteni);
    }

    /**
     * Returns friends of a given user.
     *
     * @param id user ID
     * @return sorted friend list
     */
    public List<User> getFriendsOfUser(Long id) {

        if (id == null) {
            throw new ServiceException(
                    "User does not exist."
            );
        }

        User user = userRepository.findOne(id)
                .orElseThrow(() ->
                        new ServiceException(
                                "User does not exist."
                        ));

        return user.getFriends().stream()
                .map(userRepository::findOne)
                .flatMap(optional ->
                        optional.map(Stream::of)
                                .orElseGet(Stream::empty))
                .sorted(
                        Comparator.comparing(User::getLastName)
                                .thenComparing(User::getFirstName)
                )
                .collect(Collectors.toList());
    }
}

