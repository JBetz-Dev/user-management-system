import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserService us = new UserService();

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Welcome to the User Management System!");
            while (true) {
                System.out.printf("""
                Please choose one of the following options:
                1. Create new user
                2. List all users
                3. Update username
                4. Delete user
                5. Exit
                """);
                int selectedOption = sc.nextInt();
                switch (selectedOption) {
                    case 1:
                        System.out.println("Username:");
                        String username = sc.next();
                        System.out.println("Email:");
                        String email = sc.next();
                        System.out.println("Password:");
                        String password = sc.next();
                        System.out.println("Creating new user...");
                        if (us.getUserByUsername(username) == null) {
                            if (us.registerNewUser(username, email, password) != null) {
                                System.out.println("User created successfully!");
                            } else {
                                System.out.println("Error encountered while creating user!");
                            }
                        } else {
                            System.out.println("User already exists!");
                        }
                        break;
                    case 2:
                        List<User> users = us.getAllUsers();
                        if (users.isEmpty()) {
                            System.out.println("No users found!");
                        } else {
                            System.out.println("User List:");
                            for (User user : users) {
                                System.out.println("- " + user.getUsername());
                            }
                        }
                        break;
                    case 3:
                        System.out.println("Enter the username you want to update:");
                        String usernameToModify = sc.next();
                        User userToModify = us.getUserByUsername(usernameToModify);
                        if (userToModify != null) {
                            int passwordAttempts = 0;
                            while (passwordAttempts < 5) {
                                System.out.println("Enter the password for this user:");
                                String passwordToValidate = sc.next();
                                passwordAttempts++;
                                if (userToModify.verifyPassword(passwordToValidate)) {
                                    System.out.println("Password validated successfully!");
                                    System.out.println("Enter new username:");
                                    String newUsername = sc.next();
                                    if (us.changeUsername(userToModify, newUsername, passwordToValidate)) {
                                        System.out.println("Username changed successfully!");
                                    } else {
                                        System.out.println("Error encountered while changing username!");
                                    }
                                    break;
                                } else {
                                    System.out.println("Invalid password! (" + (5 - passwordAttempts) + " remaining)");
                                }
                            }
                            if (passwordAttempts >= 5) {
                                System.out.println("Exceeded maximum password attempts! Returning to main menu...");
                            }
                        } else {
                            System.out.println("User does not exist!");
                        }
                        break;
                    case 4:
                        System.out.println("Enter the username you want to delete:");
                        String usernameToDelete = sc.next();
                        User  userToDelete = us.getUserByUsername(usernameToDelete);
                        if (userToDelete != null) {
                            int passwordAttempts = 0;
                            while (passwordAttempts < 5) {
                                System.out.println("Enter the password for this user:");
                                String passwordToValidate = sc.next();
                                passwordAttempts++;
                                if (userToDelete.verifyPassword(passwordToValidate)) {
                                    System.out.println("Password validated successfully!");
                                    System.out.println("Deleting user...");
                                    if (us.removeUserById(userToDelete.getId())) {
                                        System.out.println("User deleted successfully!");
                                    } else {
                                        System.out.println("Error encountered while deleting user!");
                                    }
                                    break;
                                } else {
                                    System.out.println("Invalid password! (" + (5 - passwordAttempts) + " remaining)");
                                }
                            }
                            if (passwordAttempts >= 5) {
                                System.out.println("Exceeded maximum password attempts! Returning to main menu...");
                            }
                        } else {
                            System.out.println("User does not exist!");
                        }
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid input!");
                        break;
                }
            }
        }
    }
}