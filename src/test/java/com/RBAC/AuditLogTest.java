package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void testLogEntry() {
        auditLog.log("USER_CREATE", "admin", "john_doe", "Created user John Doe");

        auditLog.flush();

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditLog.AuditEntry entry = entries.get(0);

        assertEquals("USER_CREATE", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("john_doe", entry.target());
        assertEquals("Created user John Doe", entry.details());
        assertNotNull(entry.timestamp());
    }

    @Test
    void testGetByPerformer() {
        auditLog.log("USER_CREATE", "admin", "john", "Created");
        auditLog.log("ROLE_CREATE", "admin", "manager", "Created");
        auditLog.log("USER_DELETE", "john", "jane", "Deleted");

        auditLog.flush();

        List<AuditLog.AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        List<AuditLog.AuditEntry> johnEntries = auditLog.getByPerformer("john");

        assertEquals(2, adminEntries.size());
        assertEquals(1, johnEntries.size());

        assertEquals("USER_DELETE", johnEntries.get(0).action());
    }

    @Test
    void testGetByAction() {
        auditLog.log("USER_CREATE", "admin", "john", "Created");
        auditLog.log("USER_CREATE", "admin", "jane", "Created");
        auditLog.log("ROLE_CREATE", "admin", "manager", "Created");

        auditLog.flush();

        List<AuditLog.AuditEntry> userCreateEntries = auditLog.getByAction("USER_CREATE");
        List<AuditLog.AuditEntry> roleCreateEntries = auditLog.getByAction("ROLE_CREATE");

        assertEquals(2, userCreateEntries.size());
        assertEquals(1, roleCreateEntries.size());
    }

    @Test
    void testSaveToFile(@TempDir Path tempDir) throws IOException {
        auditLog.log("USER_CREATE", "admin", "john", "Created user");
        auditLog.log("ROLE_CREATE", "admin", "manager", "Created role");

        auditLog.flush();

        Path filePath = tempDir.resolve("audit.log");

        auditLog.saveToFile(filePath.toString());

        assertTrue(Files.exists(filePath));

        List<String> lines = Files.readAllLines(filePath);
        
        assertEquals(3, lines.size());

        assertTrue(lines.get(0).contains("Timestamp,Action,Performer,Target,Details"));
    }

    @Test
    void testEmptyLog() {
        auditLog.flush();

        assertTrue(auditLog.getAll().isEmpty());
        assertTrue(auditLog.getByPerformer("admin").isEmpty());
        assertTrue(auditLog.getByAction("USER_CREATE").isEmpty());
    }
}