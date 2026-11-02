package repository;

/**
 * Defines the supported repository persistence types.
 */
public enum RepositoryType {

    /** Stores entities only while the application runs. */
    MEMORY,

    /** Persists entities in UTF-8 text files. */
    FILE,

    /** Database placeholders to be implemented in Lab 4. */
    DATABASE
}
