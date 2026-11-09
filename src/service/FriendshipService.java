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

/**
 * Provides application operations for undirected friendships.
 */
public class FriendshipService {

    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;
    private final Validator<Friendship> validator;

    /**
     * Creates the friendship service.
     *
     * @param userRepository user repository
     * @param friendshipRepository friendship repository
     * @param validator friendship validation strategy
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
     * Creates a new friendship using the current local date and time.
     *
     * @param id1 first user identifier
     * @param id2 second user identifier
     * @throws exceptions.ValidationException if the friendship is invalid
     * @throws ServiceException if either user is missing or friendship exists
     */
    public void addFriendship(Long id1, Long id2) {

        Friendship friendship = new Friendship(
                id1,
                id2,
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        );

        validator.validate(friendship);

        User firstUser = userRepository.findOne(id1)
                .orElseThrow(() ->
                        new ServiceException("First user does not exist."));

        User secondUser = userRepository.findOne(id2)
                .orElseThrow(() ->
                        new ServiceException("Second user does not exist."));

        if (friendshipRepository.findOne(friendship.getId()).isPresent()) {
            throw new ServiceException("Friendship already exists.");
        }

        friendshipRepository.save(friendship);

        firstUser.addFriend(id2);
        secondUser.addFriend(id1);
    }

    /**
     * Deletes an undirected friendship.
     *
     * @param id1 first user identifier
     * @param id2 second user identifier
     * @throws ServiceException if the friendship does not exist
     */
    public void removeFriendship(Long id1, Long id2) {

        Tuple<Long, Long> friendshipId =
                Friendship.normalizedId(id1, id2);

        if (friendshipId == null
                || !friendshipRepository.delete(friendshipId)) {

            throw new ServiceException(
                    "Friendship does not exist."
            );
        }

        userRepository.findOne(id1)
                .ifPresent(user -> user.removeFriend(id2));

        userRepository.findOne(id2)
                .ifPresent(user -> user.removeFriend(id1));
    }

    /**
     * Returns all friendships.
     *
     * @return list of stored friendships
     */
    public List<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
    }
}
