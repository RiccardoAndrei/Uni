import domain.Friendship;
import domain.Tuple;
import domain.User;

import exceptions.RepositoryException;
import exceptions.ValidationException;

import repository.Repository;
import repository.RepositoryType;
import repository.factory.RepositoryFactory;

import service.CommunityService;
import service.FriendshipService;
import service.UserService;

import ui.ConsoleUI;

import validation.FriendshipValidator;
import validation.UserValidator;
import validation.Validator;

/**
 * Application entry point for Laboratory 5.
 */
public class Main {

    private Main() {
    }

    /**
     * Creates repositories and services and starts the UI.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {

        try {
            RepositoryFactory factory =
                    new RepositoryFactory();

            boolean databaseMode =
                    factory.getType() == RepositoryType.DATABASE;

            Validator<User> userValidator =
                    new UserValidator(databaseMode)
                            .andThen(user -> {

                                if (user.getFirstName().length() > 50
                                        || user.getLastName().length() > 50) {

                                    throw new ValidationException(
                                            "Names must have at most "
                                                    + "50 characters."
                                    );
                                }
                            });

            Repository<Long, User> userRepository =
                    factory.createUtilizatorRepository(
                            userValidator
                    );

            Repository<Tuple<Long, Long>, Friendship>
                    friendshipRepository =
                    factory.createPrietenieRepository(
                            new FriendshipValidator()
                    );

            UserService userService = new UserService(
                    userRepository,
                    friendshipRepository,
                    userValidator
            );

            FriendshipService friendshipService =
                    new FriendshipService(
                            userRepository,
                            friendshipRepository,
                            new FriendshipValidator()
                    );

            CommunityService communityService =
                    new CommunityService(
                            userRepository,
                            friendshipRepository
                    );

            System.out.println(
                    "Selected repository type: "
                            + factory.getType()
            );

            ConsoleUI ui = new ConsoleUI(
                    userService,
                    friendshipService,
                    communityService,
                    databaseMode
            );

            ui.run();

        } catch (RepositoryException e) {
            System.err.println(
                    "Startup error: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            System.err.println(
                    "Invalid configuration: " + e.getMessage()
            );
        }
    }
}