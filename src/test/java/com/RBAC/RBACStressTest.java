package com.RBAC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RBACStressTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    private List<Role> roles;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager();

        roles = new ArrayList<>();

        Role admin = new Role("Admin", "admin");
        Role user = new Role("User", "user");
        Role manager = new Role("Manager", "manager");

        roleManager.add(admin);
        roleManager.add(user);
        roleManager.add(manager);

        roles.add(admin);
        roles.add(user);
        roles.add(manager);
    }

    @Test
    void stressTestConcurrentOperations() throws InterruptedException {

        int threads = 10;
        int operationsPerThread = 200;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        AtomicInteger createdUsers = new AtomicInteger();
        AtomicInteger assignedRoles = new AtomicInteger();
        AtomicInteger searchOps = new AtomicInteger();

        System.out.println("START STRESS TEST");

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

                                User user = new User(
                                        username,
                                        "Test User " + i,
                                        username + "@mail.com"
                                );

                                userManager.add(user);
                                createdUsers.incrementAndGet();
                            }

                            case 1 -> {
                                List<User> users = userManager.findAll();

                                if (!users.isEmpty()) {
                                    User u = users.get(random.nextInt(users.size()));
                                    Role r = roles.get(random.nextInt(roles.size()));

                                    RoleAssignment assignment =
                                            new PermanentAssignment(
                                                    u,
                                                    r,
                                                    AssignmentMetadata.now("stress-test", "load test")
                                            );

                                    assignmentManager.add(assignment);
                                    assignedRoles.incrementAndGet();
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

        assertTrue(userManager.count() > 0);
        assertTrue(roleManager.count() == 3);
    }

    private void validateIntegrity(UserManager userManager,
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

        int missingUsers = 0;

        for (RoleAssignment a : assignments) {
            if (!users.contains(a.user())) {
                missingUsers++;
            }
        }

        System.out.println("Duplicate users: " + duplicateUsers);
        System.out.println("Broken assignments: " + brokenAssignments);
        System.out.println("Assignments with missing users: " + missingUsers);

        assertEquals(0, duplicateUsers);
        assertEquals(0, brokenAssignments);
    }
}