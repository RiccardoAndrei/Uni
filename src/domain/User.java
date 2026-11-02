package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A social-network user with a list of friend identifiers.
 */
public class User extends Entity<Long> {

    private String firstName;
    private String lastName;
    private final List<Long> friends;

    /**
     * Constructs a user.
     *
     * @param id unique identifier
     * @param firstName given name
     * @param lastName family name
     */
    public User(Long id, String firstName, String lastName) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.friends = new ArrayList<Long>();
    }

    /**
     * Returns the first name.
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     *
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Changes the first name.
     *
     * @param firstName new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Changes the last name.
     *
     * @param lastName new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns a read-only copy of the friend identifiers.
     *
     * @return friend identifiers
     */
    public List<Long> getFriends() {
        return Collections.unmodifiableList(
                new ArrayList<Long>(friends)
        );
    }

    /**
     * Adds a friend if not already present.
     *
     * @param friendId friend identifier
     */
    public void addFriend(Long friendId) {
        if (!friends.contains(friendId)) {
            friends.add(friendId);
        }
    }

    /**
     * Removes a friend.
     *
     * @param friendId friend identifier
     */
    public void removeFriend(Long friendId) {
        friends.remove(friendId);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "User{id=" + getId()
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", friends=" + friends + '}';
    }
}
