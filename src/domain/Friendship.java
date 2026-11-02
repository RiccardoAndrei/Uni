package domain;

import java.time.LocalDateTime;

/**
 * An undirected friendship identified by a normalized pair of user IDs.
 */
public class Friendship extends Entity<Tuple<Long, Long>> {

    private final Long userId1;
    private final Long userId2;
    private final LocalDateTime friendshipDate;

    /**
     * Constructs a friendship and normalizes its endpoints.
     * The smaller non-null identifier is stored first.
     *
     * @param userId1 first user identifier
     * @param userId2 second user identifier
     * @param friendshipDate creation date in ISO local date-time format
     */
    public Friendship(Long userId1, Long userId2,
                      LocalDateTime friendshipDate) {

        super(normalizedId(userId1, userId2));

        if (userId1 != null && userId2 != null
                && userId1.compareTo(userId2) > 0) {
            this.userId1 = userId2;
            this.userId2 = userId1;
        } else {
            this.userId1 = userId1;
            this.userId2 = userId2;
        }

        this.friendshipDate = friendshipDate;
    }

    /**
     * Creates the canonical identifier for an undirected friendship.
     *
     * @param id1 first user identifier
     * @param id2 second user identifier
     * @return normalized pair, or null if either identifier is null
     */
    public static Tuple<Long, Long> normalizedId(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            return null;
        }

        if (id1.compareTo(id2) <= 0) {
            return new Tuple<Long, Long>(id1, id2);
        }

        return new Tuple<Long, Long>(id2, id1);
    }

    /**
     * Returns the lower endpoint ID.
     *
     * @return first user identifier
     */
    public Long getUserId1() {
        return userId1;
    }

    /**
     * Returns the higher endpoint ID.
     *
     * @return second user identifier
     */
    public Long getUserId2() {
        return userId2;
    }

    /**
     * Returns the date on which friendship was created.
     *
     * @return friendship date
     */
    public LocalDateTime getFriendshipDate() {
        return friendshipDate;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Friendship{" + userId1 + " <-> " + userId2
                + ", date=" + friendshipDate + '}';
    }
}