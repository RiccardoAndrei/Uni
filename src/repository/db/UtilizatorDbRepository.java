package repository.db;

import domain.User;
import repository.Repository;
import validation.Validator;

import java.util.List;
import java.util.Optional;

/**
 * lab 4 ONLY
 */
public class UtilizatorDbRepository
        implements Repository<Long, User> {

    private final Validator<User> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Configures the future database repository.
     *
     * @param validator user validator
     * @param url JDBC URL
     * @param user database username
     * @param password database password
     */
    public UtilizatorDbRepository(
            Validator<User> validator,
            String url,
            String user,
            String password) {

        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** {@inheritDoc} */
    @Override
    public void save(User entity) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findOne(Long id) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public List<User> findAll() {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(Long id) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public void update(User entity) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }
}