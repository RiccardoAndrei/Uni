package service;

import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.ServiceException;
import repository.Repository;
import validation.Validator;

import java.util.List;
import java.util.Optional;

/**
 * Application logic for user creation, deletion and listing.
 * Deleting a user also removes every associated friendship.
 */
public class UserService {

    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;
    private final Validator<User> validator;

    /**
     * Creates a user service with injected dependencies.
     *
     * @param userRepository user storage abstraction
     * @param friendshipRepository friendship storage abstraction
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
     * Validates and creates a user.
     *
     * @param id new user identifier
     * @param firstName first name
     * @param lastName last name
     * @throws exceptions.ValidationException when the user is invalid
     * @throws exceptions.RepositoryException when the ID already exists
     *         or persistence fails
     */
    public void addUser(Long id, String firstName, String lastName) {
        User user = new User(id, firstName, lastName);

        validator.validate(user);
        userRepository.save(user);
    }

    /**
     * Deletes the user and all incident friendships.
     *
     * @param id identifier to remove
     * @throws ServiceException when the user does not exist
     * @throws exceptions.RepositoryException when persistence fails
     */
    public void removeUser(Long id) {

        if (id == null || userRepository.findOne(id).isEmpty()) {
            throw new ServiceException("User does not exist.");
        }

        List<Friendship> friendships = friendshipRepository.findAll();

        for (Friendship friendship : friendships) {

            if (friendship.getUserId1().equals(id) ||
                    friendship.getUserId2().equals(id)) {

                Long otherId;

                if (friendship.getUserId1().equals(id)) {
                    otherId = friendship.getUserId2();
                } else {
                    otherId = friendship.getUserId1();
                }

                friendshipRepository.delete(friendship.getId());

                Optional<User> otherUser = userRepository.findOne(otherId);

                if (otherUser.isPresent()) {
                    otherUser.get().removeFriend(id);
                }
            }
        }

        userRepository.delete(id);
    }

    /**
     * Lists all users.
     *
     * @return snapshot of stored users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
