package com.RBAC;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            if (required && input.isEmpty()) {
                System.out.println(ANSI_RED + "Ошибка: поле обязательно для заполнения" + ANSI_RESET);
                continue;
            }

            if (!required && input.isEmpty()) {
                return null;
            }

            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println(ANSI_RED + String.format("Ошибка: введите число от %d до %d", min, max) + ANSI_RESET);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Ошибка: введите корректное число" + ANSI_RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("да") || input.equals("yes") || input.equals("y") || input.equals("д")) {
                return true;
            }

            if (input.equals("нет") || input.equals("no") || input.equals("n") || input.equals("н")) {
                return false;
            }

            System.out.println(ANSI_RED + "Ошибка: введите 'да' или 'нет'" + ANSI_RESET);
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Нет доступных опций" + ANSI_RESET);
            return null;
        }

        while (true) {
            System.out.println(message);

            // Выводим опции с номерами
            for (int i = 0; i < options.size(); i++) {
                T option = options.get(i);
                System.out.printf("  %d. %s%n", i + 1, formatOption(option));
            }

            System.out.print("Выберите номер (1-" + options.size() + "): ");

            try {
                String input = scanner.nextLine().trim();
                int choice = Integer.parseInt(input);

                if (choice < 1 || choice > options.size()) {
                    System.out.println(ANSI_RED + String.format("Ошибка: введите число от 1 до %d", options.size()) + ANSI_RESET);
                    continue;
                }

                return options.get(choice - 1);

            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Ошибка: введите корректное число" + ANSI_RESET);
            }
        }
    }

    public static void printSuccess(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    public static void printError(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    public static void printWarning(String message) {
        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    public static void printInfo(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    private static <T> String formatOption(T option) {
        if (option instanceof User u) {
            return String.format("%s (%s)", u.username(), u.fullName());
        } else if (option instanceof Role r) {
            return String.format("%s", r.getName());
        } else if (option instanceof Permission p) {
            return String.format("%s on %s: %s", p.name(), p.resource(), p.description());
        } else if (option instanceof RoleAssignment ra) {
            return String.format("%s - %s (%s)", ra.role().getName(),
                    ra.user().username(), ra.isActive() ? "active" : "expired");
        } else {
            return option.toString();
        }
    }

    public static void pause(Scanner scanner) {
        System.out.print("\nНажмите Enter для продолжения...");
        scanner.nextLine();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(ANSI_GREEN + title + ANSI_RESET);
        System.out.println("=".repeat(60));
    }
}