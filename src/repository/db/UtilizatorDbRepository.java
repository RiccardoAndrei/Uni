package repository.db;

import domain.User;
import exceptions.RepositoryException;
import repository.Repository;
import validation.Validator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for users using JDBC.
 */
public class UtilizatorDbRepository
        implements Repository<Long, User> {

    private final Validator<User> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a user database repository.
     *
     * @param validator user validator
     * @param url JDBC URL
     * @param username database username
     * @param password database password
     */
    public UtilizatorDbRepository(
            Validator<User> validator,
            String url,
            String username,
            String password) {

        this.validator = validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(
                url, username, password
        );
    }

    private RepositoryException sqlError(
            String operation, SQLException e) {

        String state = e.getSQLState();
        String message;

        if (state != null && state.startsWith("08")) {
            message = "Cannot connect to PostgreSQL.";
        } else if ("23505".equals(state)) {
            message = "Duplicate database value.";
        } else if ("23503".equals(state)) {
            message = "Referenced user does not exist.";
        } else if ("23514".equals(state)) {
            message = "Database constraint violated.";
        } else {
            message = "SQL error: " + e.getMessage();
        }

        return new RepositoryException(
                operation + ": " + message, e
        );
    }

    private void setUserFields(
            PreparedStatement ps, User user)
            throws SQLException {

        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getCity());
        ps.setString(4, user.getEmail());

        if (user.getBirthDate() == null) {
            ps.setNull(5, Types.DATE);
        } else {
            ps.setDate(
                    5, Date.valueOf(user.getBirthDate())
            );
        }
    }

    private User fromRow(
            ResultSet rs, Connection connection)
            throws SQLException {

        Date date = rs.getDate("birth_date");

        LocalDate birthDate = date == null
                ? null
                : date.toLocalDate();

        User user = new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("city"),
                rs.getString("email"),
                birthDate
        );

        String sql =
                "SELECT CASE WHEN id1 = ? THEN id2 ELSE id1 END "
                        + "AS friend_id "
                        + "FROM friendships WHERE id1 = ? OR id2 = ?";

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            ps.setLong(2, user.getId());
            ps.setLong(3, user.getId());

            try (ResultSet friends = ps.executeQuery()) {
                while (friends.next()) {
                    user.addFriend(
                            friends.getLong("friend_id")
                    );
                }
            }
        }

        return user;
    }

    /** {@inheritDoc} */
    @Override
    public void save(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "User must not be null."
            );
        }

        if (entity.getId() != null) {
            throw new IllegalArgumentException(
                    "New user ID must be null. "
                            + "PostgreSQL generates it."
            );
        }

        validator.validate(entity);

        String sql =
                "INSERT INTO users "
                        + "(first_name, last_name, city, email, birth_date) "
                        + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(
                             sql, Statement.RETURN_GENERATED_KEYS)) {

            setUserFields(ps, entity);

            int affectedRows = ps.executeUpdate();

            if (affectedRows != 1) {
                throw new RepositoryException(
                        "User was not inserted."
                );
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new RepositoryException(
                            "Generated user ID was not returned."
                    );
                }

                entity.setId(keys.getLong(1));
            }

        } catch (SQLException e) {
            throw sqlError("Cannot save user", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID must not be null."
            );
        }

        String sql =
                "SELECT id, first_name, last_name, "
                        + "city, email, birth_date "
                        + "FROM users WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                            fromRow(rs, connection)
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw sqlError("Cannot find user", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<User> findAll() {
        String sql =
                "SELECT id, first_name, last_name, "
                        + "city, email, birth_date "
                        + "FROM users ORDER BY id";

        List<User> result = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(fromRow(rs, connection));
            }

            return result;

        } catch (SQLException e) {
            throw sqlError("Cannot list users", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public User delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID must not be null."
            );
        }

        String sql =
                "DELETE FROM users WHERE id = ? "
                        + "RETURNING id, first_name, last_name, "
                        + "city, email, birth_date";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Date date = rs.getDate("birth_date");

                return new User(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("city"),
                        rs.getString("email"),
                        date == null
                                ? null
                                : date.toLocalDate()
                );
            }

        } catch (SQLException e) {
            throw sqlError("Cannot delete user", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public User update(User entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "User and ID must not be null."
            );
        }

        validator.validate(entity);

        String sql =
                "UPDATE users SET "
                        + "first_name = ?, last_name = ?, "
                        + "city = ?, email = ?, birth_date = ? "
                        + "WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            setUserFields(ps, entity);
            ps.setLong(6, entity.getId());

            int affectedRows = ps.executeUpdate();

            return affectedRows == 0 ? entity : null;

        } catch (SQLException e) {
            throw sqlError("Cannot update user", e);
        }
    }
}