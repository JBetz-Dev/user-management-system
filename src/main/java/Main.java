import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserService us = new UserService();

        // Test User creation
        User newUser = us.registerNewUser("CoolGuy", "cool@cool.com", "password123");
        if (newUser != null) {
            System.out.println("New User registered successfully!");
        } else {
            System.out.println("Error while creating User!");
        }

        // Test User creation
        User anotherUser = us.registerNewUser("AnotherGuy", "another@guy.com", "password456");
        if (anotherUser != null) {
            System.out.println("New User registered successfully!");
        } else {
            System.out.println("Error while creating User!");
        }

        // Test updating username
        String newUsername = "CoolerGuy";
        if (us.changeUsername(newUser, newUsername)) {
            System.out.println("Username changed successfully!");
        } else {
            System.out.println("Error while changing username!");
        }

        // Test getting all users
        List<User> users = us.getAllUsers();
        System.out.println("List of Users:");
        if (users.isEmpty()) {
            System.out.println("No users found!");
        } else {
            for (User user : users) {
                System.out.println(user.getUsername());
            }
        }

        // Test deleting a user
        boolean deleted = us.removeUserById(newUser.getId());
        if (deleted) {
            System.out.println("User removed successfully!");
        } else {
            System.out.println("Error while removing user!");
        }


    }
}