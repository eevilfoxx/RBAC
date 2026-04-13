package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConsoleUtilsTest {

    private Scanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
    }

    @Test
    void testPromptStringRequired() {
        String input = "test_value\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleUtils.promptString(scanner, "Enter value: ", true);

        assertEquals("test_value", result);
    }

    @Test
    void testPromptStringRequiredWithEmptyInput() {
        String input = "\n\nvalid\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleUtils.promptString(scanner, "Enter value: ", true);

        assertEquals("valid", result);
    }

    @Test
    void testPromptStringOptional() {
        String input = "\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleUtils.promptString(scanner, "Enter value: ", false);

        assertNull(result);
    }

    @Test
    void testPromptIntValid() {
        String input = "5\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);

        assertEquals(5, result);
    }

    @Test
    void testPromptIntInvalidThenValid() {
        String input = "abc\n15\n7\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);

        assertEquals(7, result);
    }

    @Test
    void testPromptYesNoYes() {
        String input = "да\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertTrue(result);
    }

    @Test
    void testPromptYesNoNo() {
        String input = "нет\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertFalse(result);
    }

    @Test
    void testPromptYesNoInvalidThenValid() {
        String input = "maybe\nда\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertTrue(result);
    }

    @Test
    void testPromptChoice() {
        String input = "2\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");

        String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);

        assertEquals("Option 2", result);
    }

    @Test
    void testPromptChoiceInvalidThenValid() {
        String input = "5\n2\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");

        String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);

        assertEquals("Option 2", result);
    }
}