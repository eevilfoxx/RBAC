package com.RBAC;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RBACStressTest {

    public static void main(String[] args) throws InterruptedException {

        RBACSystem system = new RBACSystem();
        system.initialize();

        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();
        AssignmentManager assignmentManager = system.getAssignmentManager();

        int threads = 10;
        int operationsPerThread = 200;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger assignedRoles = new AtomicInteger();
        AtomicInteger searchOps = new AtomicInteger();

        List<Role> roles = roleManager.findAll();

        System.out.println("START STRESS TEST");

        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            int threadId = t;

            executor.submit(() -> {
                try {
                    Random random = new Random();

                    for (int i = 0; i < operationsPerThread; i++) {

                        int op = random.nextInt(3);

                        switch (op) {

                            case 0 -> {
                                String username = "user_" + threadId + "_" + i;

                                try {
                                    User u = new User(
                                            username,
                                            "Test User " + i,
                                            username + "@mail.com"
                                    );

                                    userManager.add(u);
                                    createdUsers.incrementAndGet();
                                } catch (Exception ignored) {
                                }
                            }

                            case 1 -> {
                                List<User> users = userManager.findAll();
                                if (!users.isEmpty() && !roles.isEmpty()) {

                                    User u = users.get(random.nextInt(users.size()));
                                    Role r = roles.get(random.nextInt(roles.size()));

                                    try {
                                        RoleAssignment a = new PermanentAssignment(
                                                u,
                                                r,
                                                AssignmentMetadata.now("stress-test", "load test")
                                        );

                                        assignmentManager.add(a);
                                        assignedRoles.incrementAndGet();

                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            case 2 -> {
                                List<User> users = userManager.findAll();

                                if (!users.isEmpty()) {
                                    User u = users.get(random.nextInt(users.size()));

                                    assignmentManager.findByUser(u);
                                    assignmentManager.getUserPermissions(u);

                                    searchOps.incrementAndGet();
                                }
                            }
                        }
                    }

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("\n=== STRESS TEST RESULT ===");
        System.out.println("Users created: " + createdUsers.get());
        System.out.println("Role assignments created: " + assignedRoles.get());
        System.out.println("Search operations: " + searchOps.get());

        System.out.println("\nFinal system state:");
        System.out.println("Users: " + userManager.count());
        System.out.println("Roles: " + roleManager.count());
        System.out.println("Assignments: " + assignmentManager.count());

        validateIntegrity(userManager, assignmentManager);

        System.out.println("\nSTRESS TEST COMPLETED");
    }

    private static void validateIntegrity(UserManager userManager,
                                          AssignmentManager assignmentManager) {

        System.out.println("\n=== INTEGRITY CHECK ===");

        List<User> users = userManager.findAll();
        List<RoleAssignment> assignments = assignmentManager.findAll();

        Set<String> usernames = new HashSet<>();
        int duplicateUsers = 0;

        for (User u : users) {
            if (!usernames.add(u.username())) {
                duplicateUsers++;
            }
        }

        int brokenAssignments = 0;

        for (RoleAssignment a : assignments) {
            if (a.user() == null || a.role() == null) {
                brokenAssignments++;
            }
        }

        int missingRoles = 0;

        for (RoleAssignment a : assignments) {
            boolean exists = userManager.findAll().contains(a.user());
            if (!exists) {
                missingRoles++;
            }
        }

        System.out.println("Duplicate users: " + duplicateUsers);
        System.out.println("Broken assignments: " + brokenAssignments);
        System.out.println("Assignments with missing users: " + missingRoles);
    }
}