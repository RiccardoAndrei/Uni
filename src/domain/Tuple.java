package domain;

import java.util.Objects;

/**
 * Immutable pair used as the composite identifier of a friendship.
 *
 * @param <A> first value type
 * @param <B> second value type
 */
public final class Tuple<A, B> {

    private final A first;
    private final B second;

    /**
     * Creates an ordered pair.
     *
     * @param first first element
     * @param second second element
     */
    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first element.
     *
     * @return first element
     */
    public A getFirst() {
        return first;
    }

    /**
     * Returns the second element.
     *
     * @return second element
     */
    public B getSecond() {
        return second;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Tuple<?, ?>)) {
            return false;
        }

        Tuple<?, ?> other = (Tuple<?, ?>) obj;

        return Objects.equals(first, other.first)
                && Objects.equals(second, other.second);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
