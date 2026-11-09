import domain.Friendship;
import domain.Tuple;
import domain.User;

import exceptions.RepositoryException;
import exceptions.ValidationException;

import repository.Repository;
import repository.factory.RepositoryFactory;

import service.CommunityService;
import service.FriendshipService;
import service.UserService;

import ui.ConsoleUI;

import validation.FriendshipValidator;
import validation.UserValidator;
import validation.Validator;

/**
 * Main entry point for the Lab 4 social network application.
 */
public class Main {

    private Main() {
    }

    /**
     * Initializes repositories, services and the console interface.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {

        try {
            RepositoryFactory factory = new RepositoryFactory();

            Validator<User> userValidator =
                    new UserValidator().andThen(user -> {

                        if (user.getFirstName().length() > 50
                                || user.getLastName().length() > 50) {

                            throw new ValidationException(
                                    "Names may contain at most 50 characters."
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

            FriendshipService friendshipService = new FriendshipService(
                    userRepository,
                    friendshipRepository,
                    new FriendshipValidator()
            );

            CommunityService communityService = new CommunityService(
                    userRepository,
                    friendshipRepository
            );

            System.out.println(
                    "Repository type: " + factory.getType()
            );

            ConsoleUI ui = new ConsoleUI(
                    userService,
                    friendshipService,
                    communityService
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