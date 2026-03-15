package com.RBAC;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.currentUser = null;
        this.auditLog = new AuditLog();
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
                AssignmentMetadata.now("system","Initial admin assignment"));

        assignmentManager.add(adminAssignment);
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

}