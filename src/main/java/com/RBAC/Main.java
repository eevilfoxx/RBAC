package com.RBAC;

public class Main {
    public static void main(String[] args) {
        try {
            User user1 = User.validate("john_doe", "John Doe", "john.doe@example.com");
            System.out.println("Correct: " + user1.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Incorrect: " + e.getMessage());
        }

        try {
            User user2 = User.validate("ab", "Short Name", "short@example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user3 = User.validate("toooooo_long_username", "Long Name", "long@example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user4 = User.validate("джон_доу", "Джон Доу", "ivan@example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user5 = User.validate("testuser", "Test User", "testuser@example");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user6 = User.validate("testuser", "Test User", "testuser.example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user7 = User.validate("validuser", "", "valid@example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user8 = User.validate(null, "Some Name", "email@example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        try {
            User user9 = User.validate("user-name", "Some Name", "email@example.com");
            System.out.println("Incorrect: Exception expected");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: " + e.getMessage());
        }

        // Проверка equals и hashCode
        User userA = new User("testuser", "Test User", "test@example.com");
        User userB = new User("testuser", "Test User", "test@example.com");
        User userC = new User("otheruser", "Test User", "test@example.com");

        System.out.println("userA.equals(userB): " + userA.equals(userB) + " (Expected: true)");
        System.out.println("userA.hashCode() == userB.hashCode(): " +
                (userA.hashCode() == userB.hashCode()) + " (Expected: true)");
        System.out.println("userA.equals(userC): " + userA.equals(userC) + " (Expected: false)");

    }
}