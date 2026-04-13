package com.RBAC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(100)).append("\n");
        sb.append("USER REPORT\n");
        sb.append("=".repeat(100)).append("\n\n");

        List<User> users = userManager.findAll();

        sb.append(String.format("Total users: %d\n\n", users.size()));

        sb.append(String.format("%-15s | %-25s | %-30s | %-20s | %s\n",
                "USERNAME", "FULL NAME", "EMAIL", "ROLES", "ASSIGNMENT TYPE"));
        sb.append("-".repeat(100)).append("\n");


        List<String> lines = users.parallelStream()
                .map(user -> {
                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);

                    StringBuilder local = new StringBuilder();

                    if (assignments.isEmpty()) {
                        local.append(String.format("%-15s | %-25s | %-30s | %-20s | %s\n",
                                user.username(),
                                truncate(user.fullName(), 25),
                                truncate(user.email(), 30),
                                "No roles",
                                "-"));
                    } else {
                        boolean first = true;
                        for (RoleAssignment assignment : assignments) {
                            if (first) {
                                local.append(String.format("%-15s | %-25s | %-30s | %-20s | %s\n",
                                        user.username(),
                                        truncate(user.fullName(), 25),
                                        truncate(user.email(), 30),
                                        truncate(assignment.role().getName(), 20),
                                        assignment.assignmentType() +
                                                (assignment.isActive() ? " (active)" : " (expired)")));
                                first = false;
                            } else {
                                local.append(String.format("%-15s | %-25s | %-30s | %-20s | %s\n",
                                        "", "", "",
                                        truncate(assignment.role().getName(), 20),
                                        assignment.assignmentType() +
                                                (assignment.isActive() ? " (active)" : " (expired)")));
                            }
                        }
                    }

                    local.append("-".repeat(100)).append("\n");
                    return local.toString();
                })
                .collect(Collectors.toList());

        lines.forEach(sb::append);

        sb.append("\n").append("=".repeat(100)).append("\n");
        sb.append("STATISTICS\n");
        sb.append("=".repeat(100)).append("\n");

        long usersWithRoles = users.parallelStream()
                .filter(u -> !assignmentManager.findByUser(u).isEmpty())
                .count();

        long totalAssignments = assignmentManager.findAll().size();
        long activeAssignments = assignmentManager.findAll().stream()
                .filter(RoleAssignment::isActive)
                .count();

        sb.append(String.format("Users with roles: %d (%.1f%%)\n",
                usersWithRoles, users.isEmpty() ? 0 : (usersWithRoles * 100.0 / users.size())));
        sb.append(String.format("Users without roles: %d\n", users.size() - usersWithRoles));
        sb.append(String.format("Total assignments: %d\n", totalAssignments));
        sb.append(String.format("Active assignments: %d\n", activeAssignments));

        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append("ROLE REPORT\n");
        sb.append("=".repeat(80)).append("\n\n");

        List<Role> roles = roleManager.findAll();

        sb.append(String.format("Total roles: %d\n\n", roles.size()));

        sb.append(String.format("%-20s | %-30s | %-15s | %-15s | %s\n",
                "ROLE NAME", "DESCRIPTION", "PERMISSIONS", "ASSIGNED USERS", "ACTIVE USERS"));
        sb.append("-".repeat(100)).append("\n");

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            Set<User> assignedUsers = assignments.stream()
                    .map(RoleAssignment::user)
                    .collect(Collectors.toSet());

            long activeUsers = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .map(RoleAssignment::user)
                    .distinct()
                    .count();

            sb.append(String.format("%-20s | %-15d | %-15d | %d\n",
                    truncate(role.getName(), 20),
                    role.getPermissions().size(),
                    assignedUsers.size(),
                    activeUsers));

            if (!assignedUsers.isEmpty() && assignedUsers.size() <= 10) {
                String users = assignedUsers.stream()
                        .map(User::username)
                        .collect(Collectors.joining(", "));
                sb.append(String.format("%-20s | %-30s | %-15s | Users: %s\n",
                        "", "", "", users));
            }

            sb.append("-".repeat(100)).append("\n");
        }

        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(120)).append("\n");
        sb.append("PERMISSION MATRIX (Users × Resources)\n");
        sb.append("=".repeat(120)).append("\n\n");

        List<User> users = userManager.findAll();

        Map<String, Set<String>> userPermissions = users.parallelStream()
                .collect(Collectors.toConcurrentMap(
                        User::username,
                        user -> {
                            Set<String> permissions = new HashSet<>();
                            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

                            for (RoleAssignment assignment : assignments) {
                                if (assignment.isActive()) {
                                    for (Permission perm : assignment.role().getPermissions()) {
                                        permissions.add(perm.resource() + ":" + perm.name());
                                    }
                                }
                            }
                            return permissions;
                        }
                ));

        Set<String> allResources = users.parallelStream()
                .flatMap(user -> assignmentManager.findByUser(user).stream())
                .filter(RoleAssignment::isActive)
                .flatMap(a -> a.role().getPermissions().stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(TreeSet::new));

        List<String> resources = new ArrayList<>(allResources);

        sb.append(String.format("%-15s", "USERNAME"));
        for (String resource : resources) {
            sb.append(String.format(" | %-15s", truncate(resource, 15)));
        }
        sb.append("\n");
        sb.append("-".repeat(15 + resources.size() * 18)).append("\n");

        List<String> lines = users.parallelStream()
                .map(user -> {
                    StringBuilder local = new StringBuilder();

                    local.append(String.format("%-15s", truncate(user.username(), 15)));

                    Set<String> userPerms = userPermissions.getOrDefault(user.username(), Set.of());

                    for (String resource : resources) {
                        boolean hasRead = userPerms.contains(resource + ":read") ||
                                userPerms.contains(resource + ":write") ||
                                userPerms.contains(resource + ":all");

                        boolean hasWrite = userPerms.contains(resource + ":write") ||
                                userPerms.contains(resource + ":all");

                        String permSymbol;
                        if (hasRead && hasWrite) permSymbol = "     RW     ";
                        else if (hasRead) permSymbol = "     R      ";
                        else if (hasWrite) permSymbol = "     W      ";
                        else permSymbol = "     -      ";

                        local.append(String.format(" | %s", permSymbol));
                    }

                    local.append("\n");
                    return local.toString();
                })
                .collect(Collectors.toList());

        lines.forEach(sb::append);

        sb.append("\n").append("=".repeat(120)).append("\n");
        sb.append("LEGEND:\n");
        sb.append("  R  - Read permission\n");
        sb.append("  W  - Write permission\n");
        sb.append("  RW - Read and Write permissions\n");
        sb.append("  -  - No permissions\n");

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try {
            Path file = Paths.get(filename);
            Files.write(file, report.getBytes());
            System.out.println("Report saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}