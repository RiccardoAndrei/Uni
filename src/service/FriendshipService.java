package service;

import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.ServiceException;
import repository.Repository;
import validation.Validator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Application logic for creating, deleting and listing undirected friendships.
 */
public class FriendshipService {

    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;
    private final Validator<Friendship> validator;

    /**
     * Creates a friendship service with injected dependencies.
     *
     * @param userRepository user repository
     * @param friendshipRepository friendship repository
     * @param validator friendship validator
     */
    public FriendshipService(
            Repository<Long, User> userRepository,
            Repository<Tuple<Long, Long>, Friendship> friendshipRepository,
            Validator<Friendship> validator) {

        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.validator = validator;
    }

    /**
     * Adds an undirected friendship dated at the current local time.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @throws exceptions.ValidationException when IDs are invalid
     * @throws ServiceException when a user is missing or friendship exists
     * @throws exceptions.RepositoryException if saving fails
     */
    public void addFriendship(Long userId1, Long userId2) {

        Friendship friendship = new Friendship(
                userId1,
                userId2,
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        );

        validator.validate(friendship);

        Optional<User> first = userRepository.findOne(userId1);
        Optional<User> second = userRepository.findOne(userId2);

        if (first.isEmpty() || second.isEmpty()) {
            throw new ServiceException(
                    "Both users must exist."
            );
        }

        if (friendshipRepository.findOne(friendship.getId()).isPresent()) {
            throw new ServiceException(
                    "Friendship already exists."
            );
        }

        friendshipRepository.save(friendship);

        first.get().addFriend(userId2);
        second.get().addFriend(userId1);
    }

    /**
     * Removes an undirected friendship, regardless of endpoint order.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @throws ServiceException if the friendship does not exist
     * @throws exceptions.RepositoryException if persistence fails
     */
    public void removeFriendship(Long userId1, Long userId2) {

        Tuple<Long, Long> id =
                Friendship.normalizedId(userId1, userId2);

        if (id == null || !friendshipRepository.delete(id)) {
            throw new ServiceException(
                    "Friendship does not exist."
            );
        }

        Optional<User> first = userRepository.findOne(userId1);
        Optional<User> second = userRepository.findOne(userId2);

        if (first.isPresent()) {
            first.get().removeFriend(userId2);
        }

        if (second.isPresent()) {
            second.get().removeFriend(userId1);
        }
    }

    /**
     * Lists all friendships.
     *
     * @return snapshot of stored friendships
     */
    public List<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
    }
}
