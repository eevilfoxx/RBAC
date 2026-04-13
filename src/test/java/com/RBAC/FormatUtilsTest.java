package com.RBAC;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void testFormatTable() {
        String[] headers = {"Name", "Age", "City"};
        List<String[]> rows = Arrays.asList(
                new String[]{"John Doe", "30", "New York"},
                new String[]{"Jane Smith", "25", "Los Angeles"},
                new String[]{"Bob Johnson", "35", "Chicago"}
        );

        String table = FormatUtils.formatTable(headers, rows);

        assertNotNull(table);
        assertTrue(table.contains("Name"));
        assertTrue(table.contains("Age"));
        assertTrue(table.contains("City"));
        assertTrue(table.contains("John Doe"));
        assertTrue(table.contains("Jane Smith"));
        assertTrue(table.contains("Bob Johnson"));
        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
    }

    @Test
    void testFormatBox() {
        String text = "Test Message";

        String box = FormatUtils.formatBox(text);

        assertNotNull(box);
        assertTrue(box.contains("╔"));
        assertTrue(box.contains("╗"));
        assertTrue(box.contains("╚"));
        assertTrue(box.contains("╝"));
        assertTrue(box.contains("Test Message"));
    }

    @Test
    void testFormatBoxMultiline() {
        String text = "Line 1\nLine 2\nLine 3";

        String box = FormatUtils.formatBox(text);

        assertNotNull(box);
        assertTrue(box.contains("Line 1"));
        assertTrue(box.contains("Line 2"));
        assertTrue(box.contains("Line 3"));
    }

    @Test
    void testFormatHeader() {
        String text = "Test Header";

        String header = FormatUtils.formatHeader(text);

        assertNotNull(header);
        assertTrue(header.contains("Test Header"));
        assertTrue(header.contains("┌"));
        assertTrue(header.contains("┐"));
        assertTrue(header.contains("└"));
        assertTrue(header.contains("┘"));
    }

    @Test
    void testTruncate() {
        assertEquals("Hello...", FormatUtils.truncate("Hello World", 8));
        assertEquals("Hello World", FormatUtils.truncate("Hello World", 20));
        assertEquals("", FormatUtils.truncate(null, 10));
        assertEquals("...", FormatUtils.truncate("Hello", 3));
        assertEquals("..", FormatUtils.truncate("Hello", 2));
    }

    @Test
    void testPadRight() {
        assertEquals("Hello     ", FormatUtils.padRight("Hello", 10));
        assertEquals("Hello", FormatUtils.padRight("Hello", 5));
        assertEquals("Hello", FormatUtils.padRight("Hello", 3));
        assertEquals("     ", FormatUtils.padRight(null, 5));
    }

    @Test
    void testPadLeft() {
        assertEquals("     Hello", FormatUtils.padLeft("Hello", 10));
        assertEquals("Hello", FormatUtils.padLeft("Hello", 5));
        assertEquals("Hello", FormatUtils.padLeft("Hello", 3));
        assertEquals("     ", FormatUtils.padLeft(null, 5));
    }

    @Test
    void testPadCenter() {
        assertEquals("  Hello   ", FormatUtils.padCenter("Hello", 10));
        assertEquals("Hello", FormatUtils.padCenter("Hello", 5));
        assertEquals("Hello", FormatUtils.padCenter("Hello", 3));
    }
}