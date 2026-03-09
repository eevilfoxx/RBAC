package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.mockito.Mockito.*;

class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = mock(RBACSystem.class);
    }

    @Test
    void executeRegisteredCommand() {

        Command command = mock(Command.class);

        parser.registerCommand("test", "test command", command);

        Scanner scanner = new Scanner("");

        parser.executeCommand("test", scanner, system);

        verify(command).execute(scanner, system);
    }

    @Test
    void executeUnknownCommand() {

        Scanner scanner = new Scanner("");

        parser.executeCommand("unknown", scanner, system);
    }

}