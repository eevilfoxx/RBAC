package com.RBAC;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AuditLog {

    private final List<AuditEntry> entries;
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {}

    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        System.out.println("\n" + "=".repeat(100));
        System.out.printf("%-20s | %-20s | %-15s | %-20s | %s%n",
                "TIMESTAMP", "ACTION", "PERFORMER", "TARGET", "DETAILS");
        System.out.println("=".repeat(100));

        for (AuditEntry entry : entries) {
            System.out.printf("%-20s | %-20s | %-15s | %-20s | %s%n",
                    entry.timestamp(),
                    truncate(entry.action(), 20),
                    truncate(entry.performer(), 15),
                    truncate(entry.target(), 20),
                    truncate(entry.details(), 30));
        }
        System.out.println("=".repeat(100));
        System.out.println("Total entries: " + entries.size());
    }

    public void saveToFile(String filename) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("Timestamp,Action,Performer,Target,Details");

            for (AuditEntry entry : entries) {
                lines.add(String.format("%s,%s,%s,%s,%s",
                        entry.timestamp(),
                        entry.action(),
                        entry.performer(),
                        entry.target(),
                        entry.details().replace(",", ";")));
            }

            Path file = Paths.get(filename);
            Files.write(file, lines);
            System.out.println("Audit log saved to: " + filename);

        } catch (IOException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}