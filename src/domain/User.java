
package domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a social network user with personal information
 * and a list of friend identifiers.
 */
public class User extends Entity<Long> {

    private String firstName;
    private String lastName;
    private String city;
    private String email;
    private LocalDate birthDate;

    private final List<Long> friends;

    /**
     * Creates a user using the original Lab 3 fields.
     * Used for compatibility with older data files.
     *
     * @param id user identifier
     * @param firstName first name
     * @param lastName last name
     */
    public User(Long id, String firstName, String lastName) {
        this(id, firstName, lastName, "", "", null);
    }

    /**
     * Creates a user with the extended profile information.
     *
     * @param id user identifier
     * @param firstName first name
     * @param lastName last name
     * @param city city of residence
     * @param email email address
     * @param birthDate date of birth
     */
    public User(Long id, String firstName, String lastName,
                String city, String email, LocalDate birthDate) {

        super(id);

        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.email = email;
        this.birthDate = birthDate;

        this.friends = new ArrayList<>();
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
     * Returns the city of residence.
     *
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the email address.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the date of birth.
     *
     * @return birth date
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Sets the first name.
     *
     * @param firstName new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the city of residence.
     *
     * @param city new city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Sets the email address.
     *
     * @param email new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the date of birth.
     *
     * @param birthDate new birth date
     */
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Returns a read-only copy of the friend identifiers.
     *
     * @return friend identifiers
     */
    public List<Long> getFriends() {
        return Collections.unmodifiableList(
                new ArrayList<>(friends)
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

    /**
     * Returns a text representation of the user.
     *
     * @return user information
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", city='" + city + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                ", friends=" + friends +
                '}';
    }
}

