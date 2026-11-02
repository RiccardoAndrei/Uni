package ui;

import domain.Friendship;
import domain.User;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import service.CommunityService;
import service.FriendshipService;
import service.UserService;

import java.util.List;
import java.util.Scanner;

/**
 * Console interface for social-network operations and error display.
 */
public class ConsoleUI {

    private final UserService users;
    private final FriendshipService friendships;
    private final CommunityService communities;
    private final Scanner scanner;

    /**
     * Creates the user interface.
     *
     * @param users user service
     * @param friendships friendship service
     * @param communities community analysis service
     */
    public ConsoleUI(
            UserService users,
            FriendshipService friendships,
            CommunityService communities) {

        this.users = users;
        this.friendships = friendships;
        this.communities = communities;
        this.scanner = new Scanner(System.in);
    }

    private void printMenu() {
        System.out.println("\nRetea de socializare\n");
        System.out.println("1. Add user");
        System.out.println("2. Remove user");
        System.out.println("3. Add friendship");
        System.out.println("4. Remove friendship");
        System.out.println("5. Number of communities");
        System.out.println("6. Most sociable community");
        System.out.println("7. Show users");
        System.out.println("8. Show friendships");
        System.out.println("0. Exit");
        System.out.print("Option: ");
    }

    private Long readLong(String prompt) {
        System.out.print(prompt);
        return Long.valueOf(scanner.nextLine().trim());
    }

    private void addUser() {
        Long id = readLong("User ID: ");

        System.out.print("First name: ");
        String firstName = scanner.nextLine();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine();

        users.addUser(id, firstName, lastName);

        System.out.println("User saved.");
    }

    private void removeUser() {
        users.removeUser(readLong("User ID: "));

        System.out.println(
                "User and their friendships deleted."
        );
    }

    private void addFriendship() {
        Long id1 = readLong("First user ID: ");
        Long id2 = readLong("Second user ID: ");

        friendships.addFriendship(id1, id2);

        System.out.println("Friendship saved.");
    }

    private void removeFriendship() {
        Long id1 = readLong("First user ID: ");
        Long id2 = readLong("Second user ID: ");

        friendships.removeFriendship(id1, id2);

        System.out.println("Friendship deleted.");
    }

    private void showUsers() {
        List<User> list = users.getAllUsers();

        if (list.isEmpty()) {
            System.out.println("No users.");
        }

        for (User user : list) {
            System.out.println(user);
        }
    }

    private void showFriendships() {
        List<Friendship> list = friendships.getAllFriendships();

        if (list.isEmpty()) {
            System.out.println("No friendships.");
        }

        for (Friendship friendship : list) {
            System.out.println(friendship);
        }
    }

    private void showMostSociableCommunity() {
        List<User> users = communities.getMostSociableCommunity();

        if (users.isEmpty()) {
            System.out.println("No users.");

        } else {
            System.out.println("Most sociable community:");

            for (User user : users) {
                System.out.println(user);
            }
        }
    }

    /**
     * Runs the console menu until the user chooses to exit.
     * User-facing exceptions are reported without terminating the application.
     */
    public void run() {
        boolean running = true;

        while (running) {
            printMenu();

            if (!scanner.hasNextLine()) {
                break;
            }

            String option = scanner.nextLine().trim();

            try {
                switch (option) {
                    case "1":
                        addUser();
                        break;

                    case "2":
                        removeUser();
                        break;

                    case "3":
                        addFriendship();
                        break;

                    case "4":
                        removeFriendship();
                        break;

                    case "5":
                        System.out.println(
                                "Number of communities: "
                                        + communities.getNumberOfCommunities()
                        );
                        break;

                    case "6":
                        showMostSociableCommunity();
                        break;

                    case "7":
                        showUsers();
                        break;

                    case "8":
                        showFriendships();
                        break;

                    case "0":
                        running = false;
                        System.out.println("Goodbye!");
                        break;

                    default:
                        System.out.println("Unknown menu option.");
                        break;
                }

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid numeric ID: " + e.getMessage()
                );

            } catch (ValidationException e) {
                System.out.println(
                        "Validation error: " + e.getMessage()
                );

            } catch (RepositoryException e) {
                System.out.println(
                        "Persistence error: " + e.getMessage()
                );

            } catch (ServiceException e) {
                System.out.println(
                        "Operation error: " + e.getMessage()
                );

            } catch (UnsupportedOperationException e) {
                System.out.println(
                        "Not implemented yet: " + e.getMessage()
                );

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Invalid argument: " + e.getMessage()
                );
            }
        }
    }
}
