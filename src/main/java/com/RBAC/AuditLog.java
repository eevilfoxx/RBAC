package com.RBAC;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AuditLog {

    private final List<AuditEntry> entries;
    private final BlockingQueue<AuditEntry> queue;
    private final ExecutorService executor;

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
        this.entries = Collections.synchronizedList(new ArrayList<>());
        this.queue = new LinkedBlockingQueue<>();
        this.executor = Executors.newSingleThreadExecutor();

        startWorker();
    }

    private void startWorker() {
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    AuditEntry entry = queue.take();
                    entries.add(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        queue.offer(new AuditEntry(timestamp, action, performer, target, details));
    }

    public void flush() {
        while (!queue.isEmpty()) {
            Thread.yield();
        }

        // маленький доп. буфер чтобы worker точно успел переложить
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}
    }

    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.performer().equalsIgnoreCase(performer))
                    .collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.action().equalsIgnoreCase(action))
                    .collect(Collectors.toList());
        }
    }

    public void saveToFile(String filename) {
        List<AuditEntry> snapshot = getAll();

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Timestamp,Action,Performer,Target,Details");

            for (AuditEntry entry : snapshot) {
                lines.add(String.format("%s,%s,%s,%s,%s",
                        entry.timestamp(),
                        entry.action(),
                        entry.performer(),
                        entry.target(),
                        entry.details().replace(",", ";")));
            }

            Files.write(Paths.get(filename), lines);

        } catch (IOException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}