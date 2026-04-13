package com.RBAC;

import java.util.List;

public class FormatUtils {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        int[] columnWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (row[i] != null) {
                    columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                }
            }
        }

        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2;
        }

        StringBuilder result = new StringBuilder();

        result.append("+");
        for (int width : columnWidths) {
            result.append("-".repeat(width)).append("+");
        }
        result.append("\n");

        result.append("|");
        for (int i = 0; i < headers.length; i++) {
            result.append(padCenter(headers[i], columnWidths[i])).append("|");
        }
        result.append("\n");

        result.append("+");
        for (int width : columnWidths) {
            result.append("-".repeat(width)).append("+");
        }
        result.append("\n");

        for (String[] row : rows) {
            result.append("|");
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                result.append(padRight(cell, columnWidths[i])).append("|");
            }
            result.append("\n");
        }

        result.append("+");
        for (int width : columnWidths) {
            result.append("-".repeat(width)).append("+");
        }

        return result.toString();
    }

    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxLength = 0;

        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }

        int boxWidth = maxLength + 4; // 2 пробела с каждой стороны + рамка

        StringBuilder result = new StringBuilder();

        result.append("╔").append("═".repeat(boxWidth - 2)).append("╗\n");

        for (String line : lines) {
            result.append("║ ").append(padRight(line, maxLength)).append(" ║\n");
        }

        result.append("╚").append("═".repeat(boxWidth - 2)).append("╝");

        return result.toString();
    }

    public static String formatHeader(String text) {
        return "\n" + ANSI_BOLD + ANSI_GREEN +
                "┌" + "─".repeat(text.length() + 4) + "┐\n" +
                "│  " + text + "  │\n" +
                "└" + "─".repeat(text.length() + 4) + "┘" +
                ANSI_RESET;
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        if (maxLength <= 3) return ".".repeat(maxLength);
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return " ".repeat(length - text.length()) + text;
    }

    public static String padCenter(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;

        int spaces = length - text.length();
        int leftSpaces = spaces / 2;
        int rightSpaces = spaces - leftSpaces;

        return " ".repeat(leftSpaces) + text + " ".repeat(rightSpaces);
    }
}