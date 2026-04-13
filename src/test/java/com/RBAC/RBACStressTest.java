package com.RBAC;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RBACStressTest {

    @Test
    void stressTestConcurrentOperations() throws InterruptedException {

        RBACSystem system = new RBACSystem();

        UserManager userManager = system.getUserManager();
        RoleManager roleManager = system.getRoleManager();
        AssignmentManager assignmentManager = system.getAssignmentManager();

        int threads = 10;
        int operationsPerThread = 200;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger assignedRoles = new AtomicInteger();
        AtomicInteger searchOps = new AtomicInteger();

        Role admin = new Role("Admin", "admin");
        Role manager = new Role("Manager", "manager");

        roleManager.add(admin);
        roleManager.add(manager);

        List<Role> roles = roleManager.findAll();

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
                                    userManager.add(new User(
                                            username,
                                            "Test User",
                                            username + "@mail.com"
                                    ));
                                    createdUsers.incrementAndGet();
                                } catch (Exception ignored) {}
                            }

                            case 1 -> {
                                List<User> users = userManager.findAll();

                                if (!users.isEmpty() && !roles.isEmpty()) {
                                    User u = users.get(random.nextInt(users.size()));
                                    Role r = roles.get(random.nextInt(roles.size()));

                                    try {
                                        assignmentManager.add(
                                                new PermanentAssignment(
                                                        u,
                                                        r,
                                                        AssignmentMetadata.now("stress", "test")
                                                )
                                        );
                                        assignedRoles.incrementAndGet();
                                    } catch (Exception ignored) {}
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
        System.out.println("Assignments created: " + assignedRoles.get());
        System.out.println("Search ops: " + searchOps.get());
        
        assertTrue(userManager.count() > 0);
        assertTrue(roleManager.count() >= 2);
        assertTrue(assignmentManager.count() >= 0);

        validateIntegrity(userManager, assignmentManager);
    }

    private void validateIntegrity(UserManager userManager,
                                   AssignmentManager assignmentManager) {

        List<User> users = userManager.findAll();
        List<RoleAssignment> assignments = assignmentManager.findAll();

        Set<String> seen = new HashSet<>();
        int duplicates = 0;

        for (User u : users) {
            if (!seen.add(u.username())) duplicates++;
        }

        int broken = 0;

        for (RoleAssignment a : assignments) {
            if (a.user() == null || a.role() == null) {
                broken++;
            }
        }

        System.out.println("\n=== INTEGRITY ===");
        System.out.println("Duplicate users: " + duplicates);
        System.out.println("Broken assignments: " + broken);

        assertEquals(0, duplicates);
        assertEquals(0, broken);
    }
}