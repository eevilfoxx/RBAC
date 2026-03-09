package com.RBAC;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName.toLowerCase());
        if (command != null) {
            command.execute(scanner, system);
        } else {
            System.out.println("Ошибка: команда '" + commandName + "' не найдена.");
        }
    }

    public void printHelp() {
        System.out.println("===== Список команд =====");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("%-15s : %s%n", entry.getKey(), entry.getValue());
        }
        System.out.println("========================");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.isBlank()) return;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        executeCommand(commandName, scanner, system);
    }
}