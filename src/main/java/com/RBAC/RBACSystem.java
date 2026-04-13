package com.RBAC;

import java.util.concurrent.*;
import java.time.LocalDate;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;

    private String currentUser;

    private final ExecutorService executor;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.auditLog = new AuditLog();
        this.currentUser = null;

        this.executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {

        Role adminRole = new Role("Admin", "admin");
        Role managerRole = new Role("Manager", "manager");
        Role viewerRole = new Role("Viewer", "viewer");

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        Permission read_users_permission = new Permission("READ", "USERS", "Read users permission");
        Permission write_users_permission = new Permission("WRITE", "USERS", "Write users permission");
        Permission delete_users_permission = new Permission("DELETE", "USERS", "Delete users permission");

        roleManager.addPermissionToRole(adminRole.getName(), read_users_permission);
        roleManager.addPermissionToRole(adminRole.getName(), write_users_permission);
        roleManager.addPermissionToRole(adminRole.getName(), delete_users_permission);

        User adminUser = new User("admin", "Full Name", "admin@mail.com");
        userManager.add(adminUser);

        PermanentAssignment adminAssignment = new PermanentAssignment(
                adminUser,
                adminRole,
                AssignmentMetadata.now("system", "Initial admin assignment")
        );

        assignmentManager.add(adminAssignment);
    }


    public Future<String> generateStatisticsAsync() {
        return executor.submit(this::generateStatistics);
    }

    public Future<Void> initializeAsync() {
        return executor.submit(() -> {
            initialize();
            return null;
        });
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        return executor.submit(task);
    }

    public Future<?> runAsync(Runnable task) {
        return executor.submit(task);
    }


    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("RBAC System Statistics:\n");
        sb.append(String.format("Users: %d\n", userManager.count()));
        sb.append(String.format("Roles: %d\n", roleManager.count()));
        sb.append(String.format("Assignments: %d\n", assignmentManager.count()));
        sb.append(String.format("Audit entries: %d\n", auditLog.getAll().size()));
        return sb.toString();
    }


    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}