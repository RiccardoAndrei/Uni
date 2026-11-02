package validation;

import domain.Friendship;
import exceptions.ValidationException;

/**
 * Validates an undirected friendship, including the required date.
 */
public class FriendshipValidator implements Validator<Friendship> {

    /**
     * Constructs a friendship validator.
     */
    public FriendshipValidator() {
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Friendship friendship) {

        if (friendship == null) {
            throw new ValidationException(
                    "Friendship must not be null."
            );
        }

        if (friendship.getId() == null ||
                friendship.getUserId1() == null ||
                friendship.getUserId2() == null) {

            throw new ValidationException(
                    "Friendship user IDs must not be null."
            );
        }

        if (friendship.getUserId1().equals(friendship.getUserId2())) {
            throw new ValidationException(
                    "A user cannot be friends with themselves."
            );
        }

        if (friendship.getFriendshipDate() == null) {
            throw new ValidationException(
                    "Friendship date must not be null."
            );
        }
    }
}
