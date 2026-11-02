import domain.Friendship;
import domain.Tuple;
import domain.User;
import exceptions.RepositoryException;
import repository.Repository;
import repository.factory.RepositoryFactory;
import service.CommunityService;
import service.FriendshipService;
import service.UserService;
import ui.ConsoleUI;
import validation.FriendshipValidator;
import validation.UserValidator;

/**
 * Application entry point.
 * Depends only on repository abstractions and the factory,
 * not on concrete repository implementations.
 */
public class Main {
     /**
     * Prevents instantiation of the entry-point class.
     */
    private Main() {
    }

    /**
     * Initializes the configured repositories,
     * services and console interface.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {

        try {
            RepositoryFactory factory = new RepositoryFactory();

            Repository<Long, User> userRepository =
                    factory.createUtilizatorRepository(
                            new UserValidator()
                    );

            Repository<Tuple<Long, Long>, Friendship> friendshipRepository =
                    factory.createPrietenieRepository(
                            new FriendshipValidator()
                    );

            UserService userService = new UserService(
                    userRepository,
                    friendshipRepository,
                    new UserValidator()
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

            ConsoleUI ui = new ConsoleUI(
                    userService,
                    friendshipService,
                    communityService
            );

            System.out.println(
                    "Selected repository type: " + factory.getType()
            );

            ui.run();

        } catch (RepositoryException e) {
            System.err.println(
                    "Startup configuration / persistence error: "
                            + e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            System.err.println(
                    "Startup argument error: " + e.getMessage()
            );
        }
    }
}