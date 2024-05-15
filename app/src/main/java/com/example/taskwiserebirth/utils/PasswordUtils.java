package com.example.taskwiserebirth.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    // Method to hash a password using bcrypt
    public static String hashPassword(String password) {
        // Generate a random salt
        String salt = BCrypt.gensalt();

        // Hash the password with the generated salt
        String hashedPassword = BCrypt.hashpw(password, salt);

        // Return the hashed password with the salt appended
        return hashedPassword + "," + salt;
    }

    // Method to verify a password against its hashed version
    public static boolean verifyPassword(String password, String hashedPasswordWithSalt) {
        // Split the hashed password and salt
        String[] parts = hashedPasswordWithSalt.split(",");
        if (parts.length != 2) {
            // Invalid hashed password format
            return false;
        }

        // Extract the salt
        String salt = parts[1];

        // Hash the provided password with the extracted salt
        String hashedPasswordAttempt = BCrypt.hashpw(password, salt);

        // Compare the hashed passwords
        return hashedPasswordAttempt.equals(parts[0]);
    }
}
