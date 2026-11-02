package validation;

import domain.Friendship;
import domain.User;
import exceptions.ValidationException;
import repository.Repository;

/**
 * Decorates the friendship validation strategy with an existence check.
 * Used on both new saves and lines loaded from files.
 */
public class FriendshipWithUsersValidator
        implements Validator<Friendship> {

    private final Validator<Friendship> delegate;
    private final Repository<Long, User> users;

    /**
     * Creates a validator that requires both endpoints to exist.
     *
     * @param delegate underlying friendship validator
     * @param users user repository
     */
    public FriendshipWithUsersValidator(
            Validator<Friendship> delegate,
            Repository<Long, User> users) {

        this.delegate = delegate;
        this.users = users;
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Friendship friendship) {

        delegate.validate(friendship);

        if (users.findOne(friendship.getUserId1()).isEmpty()
                || users.findOne(friendship.getUserId2()).isEmpty()) {

            throw new ValidationException(
                    "Friendship refers to a nonexistent user."
            );
        }
    }
}
