package repository.db;

import domain.Friendship;
import domain.Tuple;
import exceptions.RepositoryException;
import repository.Repository;
import validation.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for friendships using JDBC.
 */
public class PrietenieDbRepository
        implements Repository<Tuple<Long, Long>, Friendship> {

    private final Validator<Friendship> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a friendship database repository.
     *
     * @param validator friendship validator
     * @param url JDBC URL
     * @param username database username
     * @param password database password
     */
    public PrietenieDbRepository(
            Validator<Friendship> validator,
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
            message = "Friendship already exists.";
        } else if ("23503".equals(state)) {
            message = "Referenced user does not exist.";
        } else if ("23514".equals(state)) {
            message = "Invalid friendship constraint.";
        } else {
            message = "SQL error: " + e.getMessage();
        }

        return new RepositoryException(
                operation + ": " + message, e
        );
    }

    private void checkId(Tuple<Long, Long> id) {
        if (id == null
                || id.getFirst() == null
                || id.getSecond() == null) {

            throw new IllegalArgumentException(
                    "Friendship ID must not be null."
            );
        }
    }

    private Friendship fromRow(ResultSet rs)
            throws SQLException {

        Timestamp date =
                rs.getTimestamp("friends_from");

        return new Friendship(
                rs.getLong("id1"),
                rs.getLong("id2"),
                date.toLocalDateTime()
        );
    }

    /** {@inheritDoc} */
    @Override
    public void save(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Friendship must not be null."
            );
        }

        validator.validate(entity);

        String sql =
                "INSERT INTO friendships "
                        + "(id1, id2, friends_from) "
                        + "VALUES (?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getUserId1());
            ps.setLong(2, entity.getUserId2());
            ps.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            entity.getFriendshipDate()
                    )
            );

            ps.executeUpdate();

        } catch (SQLException e) {
            throw sqlError("Cannot save friendship", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Friendship> findOne(
            Tuple<Long, Long> id) {

        checkId(id);

        String sql =
                "SELECT id1, id2, friends_from "
                        + "FROM friendships "
                        + "WHERE id1 = ? AND id2 = ?";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setLong(1, id.getFirst());
            ps.setLong(2, id.getSecond());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromRow(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw sqlError("Cannot find friendship", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Friendship> findAll() {
        String sql =
                "SELECT id1, id2, friends_from "
                        + "FROM friendships ORDER BY id1, id2";

        List<Friendship> result = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(fromRow(rs));
            }

            return result;

        } catch (SQLException e) {
            throw sqlError("Cannot list friendships", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Friendship delete(Tuple<Long, Long> id) {
        checkId(id);

        String sql =
                "DELETE FROM friendships "
                        + "WHERE id1 = ? AND id2 = ? "
                        + "RETURNING id1, id2, friends_from";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setLong(1, id.getFirst());
            ps.setLong(2, id.getSecond());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? fromRow(rs)
                        : null;
            }

        } catch (SQLException e) {
            throw sqlError("Cannot delete friendship", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Friendship update(Friendship entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "Friendship and ID must not be null."
            );
        }

        validator.validate(entity);

        String sql =
                "UPDATE friendships "
                        + "SET friends_from = ? "
                        + "WHERE id1 = ? AND id2 = ?";

        try (Connection connection = connect();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            entity.getFriendshipDate()
                    )
            );
            ps.setLong(2, entity.getUserId1());
            ps.setLong(3, entity.getUserId2());

            int affectedRows = ps.executeUpdate();

            return affectedRows == 0 ? entity : null;

        } catch (SQLException e) {
            throw sqlError("Cannot update friendship", e);
        }
    }
}
