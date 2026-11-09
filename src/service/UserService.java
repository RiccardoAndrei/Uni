
package service;

import criteria.CriteriiUtilizator;
import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.ServiceException;
import exceptions.ValidationException;
import repository.Repository;
import validation.Validator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides user operations using repository abstractions
 * and Stream API.
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
     * @param validator user validation strategy
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
     * Adds a legacy user with the original three fields.
     *
     * @param id user identifier
     * @param firstName first name
     * @param lastName last name
     * @throws ValidationException if the user is invalid
     */
    public void addUser(
            Long id, String firstName, String lastName) {

        User user = new User(id, firstName, lastName);

        validator.validate(user);
        userRepository.save(user);
    }

    /**
     * Adds a new user with complete profile information.
     *
     * @param id user identifier
     * @param firstName first name
     * @param lastName last name
     * @param city city of residence
     * @param email email address
     * @param birthDate date of birth
     * @throws ValidationException if the profile is invalid
     * @throws exceptions.RepositoryException if saving fails
     */
    public void addUser(
            Long id,
            String firstName,
            String lastName,
            String city,
            String email,
            LocalDate birthDate) {

        if ((city == null || city.isBlank())
                && (email == null || email.isBlank())
                && birthDate == null) {

            throw new ValidationException(
                    "City, email and birth date are required."
            );
        }

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
    }

    /**
     * Removes a user and all associated friendships.
     *
     * @param id user identifier
     * @throws ServiceException if the user does not exist
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
                    .ifPresent(user -> user.removeFriend(id));
        });

        userRepository.delete(id);
    }

    /**
     * Returns all users.
     *
     * @return stored users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns users matching a predicate.
     *
     * @param criterion search predicate
     * @return matching users sorted by last name and first name
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
     * Returns the friends of a specific user.
     *
     * @param id user identifier
     * @return friends sorted by last name and first name
     * @throws ServiceException if the user does not exist
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

