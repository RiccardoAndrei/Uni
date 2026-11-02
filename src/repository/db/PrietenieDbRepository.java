package repository.db;

import domain.Friendship;
import domain.Tuple;
import repository.Repository;
import validation.Validator;

import java.util.List;
import java.util.Optional;

/**
 * Lab 4 ONLY
 */
public class PrietenieDbRepository
        implements Repository<Tuple<Long, Long>, Friendship> {

    private final Validator<Friendship> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Configures the future database repository.
     *
     * @param validator friendship validator
     * @param url JDBC URL
     * @param user database username
     * @param password database password
     */
    public PrietenieDbRepository(
            Validator<Friendship> validator,
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
    public void save(Friendship entity) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public List<Friendship> findAll() {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }

    /** {@inheritDoc} */
    @Override
    public void update(Friendship entity) {
        throw new UnsupportedOperationException(
                "DATABASE mode is invalid."
        );
    }
}
