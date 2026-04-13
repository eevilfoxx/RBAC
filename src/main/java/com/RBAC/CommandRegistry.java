package com.RBAC;

import java.util.*;

public class CommandRegistry {

    private final CommandParser parser;

    public CommandRegistry(RBACSystem system) {
        this.parser = new CommandParser();
        registerAllCommands();
    }

    public CommandParser getParser() {
        return parser;
    }

    private void registerAllCommands() {
        Scanner scanner = new Scanner(System.in);

        // =================== Пользователи ===================
        parser.registerCommand("user-list", "Список всех пользователей", (s, sys) -> {
            sys.getAuditLog().log("USER_LIST", sys.getCurrentUser(), "system", "Viewed all users");
            System.out.println(FormatUtils.formatHeader("Users List"));

            List<User> users = sys.getUserManager().findAll();
            String[] headers = {"Username", "Full Name", "Email"};
            List<String[]> rows = new ArrayList<>();

            for (User u : users) {
                rows.add(new String[]{
                        u.username(),
                        FormatUtils.truncate(u.fullName(), 20),
                        FormatUtils.truncate(u.email(), 30)
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
            System.out.println("Total users: " + users.size());
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Создание нового пользователя"));

            String username = ConsoleUtils.promptString(s, "username: ", true);
            String fullName = ConsoleUtils.promptString(s, "fullName: ", true);
            String email = ConsoleUtils.promptString(s, "email: ", true);

            User user = new User(username, fullName, email);
            try {
                sys.getUserManager().add(user);
                sys.getAuditLog().log("USER_CREATE", sys.getCurrentUser(), username,
                        "User created: " + fullName + ", " + email);
                System.out.println("Пользователь создан");
            }
            catch(Exception e){
                sys.getAuditLog().log("USER_CREATE_ERROR", sys.getCurrentUser(), username,
                        "Error: " + e.getMessage());
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("USER_VIEW_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }

            User u = userOpt.get();
            sys.getAuditLog().log("USER_VIEW", sys.getCurrentUser(), username, "Viewed user details");

            System.out.println(FormatUtils.formatBox("User Information"));
            System.out.println("Username: " + u.username());
            System.out.println("Full Name: " + u.fullName());
            System.out.println("Email: " + u.email());

            System.out.println("\n" + FormatUtils.formatHeader("Roles & Permissions"));

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);
            if (assignments.isEmpty()) {
                System.out.println("  Нет назначенных ролей");
            } else {
                String[] roleHeaders = {"Role", "Type", "Status", "Assigned At"};
                List<String[]> roleRows = new ArrayList<>();

                for(RoleAssignment a : assignments){
                    roleRows.add(new String[]{
                            a.role().getName(),
                            a.assignmentType(),
                            a.isActive() ? "ACTIVE" : "EXPIRED",
                            a.metadata().assignedAt()
                    });

                    if (!a.role().getPermissions().isEmpty()) {
                        System.out.println("\n  Permissions for " + a.role().getName() + ":");
                        String[] permHeaders = {"Permission", "Resource", "Description"};
                        List<String[]> permRows = new ArrayList<>();

                        for (Permission p : a.role().getPermissions()) {
                            permRows.add(new String[]{
                                    p.name(),
                                    p.resource(),
                                    FormatUtils.truncate(p.description(), 30)
                            });
                        }
                        System.out.println(FormatUtils.formatTable(permHeaders, permRows));
                    }
                }
                System.out.println(FormatUtils.formatTable(roleHeaders, roleRows));
            }
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("USER_UPDATE_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }

            User u = userOpt.get();
            System.out.println("Текущие данные: " + u.fullName() + " / " + u.email());

            String fn = ConsoleUtils.promptString(s, "Новое fullName (Enter - оставить): ", false);
            String email = ConsoleUtils.promptString(s, "Новый email (Enter - оставить): ", false);

            if (fn == null) fn = u.fullName();
            if (email == null) email = u.email();

            sys.getUserManager().update(username, fn, email);
            sys.getAuditLog().log("USER_UPDATE", sys.getCurrentUser(), username,
                    "Updated: fullName=" + fn + ", email=" + email);
            System.out.println("Данные обновлены");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("USER_DELETE_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }

            User u = userOpt.get();
            System.out.println(FormatUtils.formatBox("Внимание: будет удалён пользователь " + u.username() + " (" + u.fullName() + ")"));

            boolean confirm = ConsoleUtils.promptYesNo(s, "Подтвердите удаление");
            if (!confirm) {
                System.out.println("Удаление отменено");
                sys.getAuditLog().log("USER_DELETE_CANCELLED", sys.getCurrentUser(), username, "Deletion cancelled");
                return;
            }

            sys.getUserManager().remove(u);
            sys.getAuditLog().log("USER_DELETE", sys.getCurrentUser(), username,
                    "User deleted: " + u.fullName() + ", " + u.email());
            System.out.println("Пользователь удалён");
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Поиск пользователей"));

            List<String> searchOptions = Arrays.asList(
                    "По username",
                    "По email",
                    "По домену email",
                    "По полному имени"
            );

            String choice = ConsoleUtils.promptChoice(s, "Выберите тип поиска:", searchOptions);
            int searchType = searchOptions.indexOf(choice) + 1;

            String filter = Objects.requireNonNull(ConsoleUtils.promptString(s, "Введите фильтр: ", true)).toLowerCase();

            List<User> results;
            switch(String.valueOf(searchType)) {
                case "1":
                    results = sys.getUserManager().findByFilter(u -> u.username().toLowerCase().contains(filter));
                    break;
                case "2":
                    results = sys.getUserManager().findByFilter(u -> u.email().toLowerCase().contains(filter));
                    break;
                case "3":
                    results = sys.getUserManager().findByFilter(u -> u.email().toLowerCase().endsWith(filter));
                    break;
                case "4":
                    results = sys.getUserManager().findByFilter(u -> u.fullName().toLowerCase().contains(filter));
                    break;
                default:
                    System.out.println("Неверный выбор");
                    sys.getAuditLog().log("USER_SEARCH_FAILED", sys.getCurrentUser(), "system", "Invalid search option");
                    return;
            }

            sys.getAuditLog().log("USER_SEARCH", sys.getCurrentUser(), "system",
                    "Searched users with filter: " + filter);

            System.out.println(FormatUtils.formatHeader("Результаты поиска (" + results.size() + " найдено)"));
            String[] headers = {"Username", "Full Name", "Email"};
            List<String[]> rows = new ArrayList<>();
            for (User u : results) {
                rows.add(new String[]{
                        u.username(),
                        FormatUtils.truncate(u.fullName(), 20),
                        FormatUtils.truncate(u.email(), 30)
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        // =================== Роли ===================
        parser.registerCommand("role-list", "Список всех ролей", (s, sys) -> {
            sys.getAuditLog().log("ROLE_LIST", sys.getCurrentUser(), "system", "Viewed all roles");

            List<Role> roles = sys.getRoleManager().findAll();
            System.out.println(FormatUtils.formatHeader("Roles List (" + roles.size() + ")"));

            String[] headers = {"Role Name", "Description", "Permissions", "ID"};
            List<String[]> rows = new ArrayList<>();

            for (Role r : roles) {
                rows.add(new String[]{
                        r.getName(),
                        FormatUtils.truncate(r.getDescription(), 25),
                        String.valueOf(r.getPermissions().size()),
                        FormatUtils.truncate(r.getId(), 10)
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("role-create", "Создать роль", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Создание новой роли"));

            String name = ConsoleUtils.promptString(s, "Название роли: ", true);
            String desc = ConsoleUtils.promptString(s, "Описание: ", false);
            if (desc == null) desc = "";

            Role r = new Role(name, desc);
            sys.getRoleManager().add(r);
            sys.getAuditLog().log("ROLE_CREATE", sys.getCurrentUser(), name,
                    "Created role with description: " + desc);
            System.out.println("Роль создана");

            boolean addMore = ConsoleUtils.promptYesNo(s, "Добавить право?");
            while(addMore){
                String p_name = ConsoleUtils.promptString(s, "Permission name: ", true);
                String res = ConsoleUtils.promptString(s, "Resource: ", true);
                String p_desc = ConsoleUtils.promptString(s, "Description: ", false);
                if (p_desc == null) p_desc = "";

                Permission p = new Permission(p_name, res, p_desc);
                r.addPermission(p);
                sys.getAuditLog().log("PERMISSION_ADD", sys.getCurrentUser(), name,
                        "Added permission: " + p_name + " on " + res);
                System.out.println("Право добавлено");

                addMore = ConsoleUtils.promptYesNo(s, "Добавить ещё право?");
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                sys.getAuditLog().log("ROLE_VIEW_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();
            sys.getAuditLog().log("ROLE_VIEW", sys.getCurrentUser(), name, "Viewed role details");

            System.out.println(FormatUtils.formatBox("Role Information"));
            System.out.println("Name: " + r.getName());
            System.out.println("Description: " + r.getDescription());
            System.out.println("ID: " + r.getId());

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(r);
            System.out.println("Assigned Users: " + assignments.stream().map(a -> a.user().username()).distinct().count());

            System.out.println("\n" + FormatUtils.formatHeader("Permissions"));
            if (r.getPermissions().isEmpty()) {
                System.out.println("  No permissions");
            } else {
                String[] permHeaders = {"Permission", "Resource", "Description"};
                List<String[]> permRows = new ArrayList<>();
                for (Permission p : r.getPermissions()) {
                    permRows.add(new String[]{
                            p.name(),
                            p.resource(),
                            FormatUtils.truncate(p.description(), 40)
                    });
                }
                System.out.println(FormatUtils.formatTable(permHeaders, permRows));
            }
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("role-update", "Обновить роль (название/описание)", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();

            String newName = ConsoleUtils.promptString(s, "Новое имя (Enter - оставить): ", false);
            String newDesc = ConsoleUtils.promptString(s, "Новое описание (Enter - оставить): ", false);

            if (newName == null) newName = role.getName();
            if (newDesc == null) newDesc = role.getDescription();

            if (!newName.equals(role.getName()) || !newDesc.equals(role.getDescription())) {
                sys.getRoleManager().remove(role);
                Role new_role = new Role(newName, newDesc);
                sys.getRoleManager().add(new_role);
                sys.getAuditLog().log("ROLE_UPDATE", sys.getCurrentUser(), name,
                        "Updated to: " + newName + " - " + newDesc);
                System.out.println("Роль обновлена");
            } else {
                System.out.println("Изменений не внесено");
            }
        });

        parser.registerCommand("role-delete", "Удалить роль", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                sys.getAuditLog().log("ROLE_DELETE_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(r);

            if (!assignments.isEmpty()) {
                System.out.println(FormatUtils.formatBox("Роль назначена пользователям:"));
                assignments.stream()
                        .map(RoleAssignment::user)
                        .distinct()
                        .forEach(u -> System.out.println(" - " + u.username()));
            }

            boolean confirm = ConsoleUtils.promptYesNo(s, "Подтвердить удаление");
            if (!confirm) {
                System.out.println("Удаление отменено");
                sys.getAuditLog().log("ROLE_DELETE_CANCELLED", sys.getCurrentUser(), name, "Deletion cancelled");
                return;
            }

            sys.getRoleManager().remove(r);
            sys.getAuditLog().log("ROLE_DELETE", sys.getCurrentUser(), name,
                    "Deleted role with " + r.getPermissions().size() + " permissions");
            System.out.println("Роль удалена");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                sys.getAuditLog().log("PERMISSION_ADD_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();

            String p_name = ConsoleUtils.promptString(s, "Permission name: ", true);
            String res = ConsoleUtils.promptString(s, "Resource: ", true);
            String p_desc = ConsoleUtils.promptString(s, "Description: ", false);
            if (p_desc == null) p_desc = "";

            Permission p = new Permission(p_name, res, p_desc);
            r.addPermission(p);
            sys.getAuditLog().log("PERMISSION_ADD", sys.getCurrentUser(), name,
                    "Added permission: " + p_name + " on " + res);
            System.out.println("Право добавлено");
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                sys.getAuditLog().log("PERMISSION_REMOVE_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();
            List<Permission> perms = new ArrayList<>(r.getPermissions());

            if (perms.isEmpty()) {
                System.out.println("У роли нет прав для удаления");
                return;
            }

            System.out.println(FormatUtils.formatHeader("Выберите право для удаления"));
            String[] permHeaders = {"#", "Permission", "Resource", "Description"};
            List<String[]> permRows = new ArrayList<>();
            for(int i = 0; i < perms.size(); i++) {
                Permission p = perms.get(i);
                permRows.add(new String[]{
                        String.valueOf(i + 1),
                        p.name(),
                        p.resource(),
                        FormatUtils.truncate(p.description(), 30)
                });
            }
            System.out.println(FormatUtils.formatTable(permHeaders, permRows));

            System.out.print("Выберите номер: ");
            int idx = Integer.parseInt(s.nextLine().trim()) - 1;

            if (idx >= 0 && idx < perms.size()) {
                Permission toRemove = perms.get(idx);
                r.removePermission(toRemove);
                sys.getAuditLog().log("PERMISSION_REMOVE", sys.getCurrentUser(), name,
                        "Removed permission: " + toRemove.name() + " on " + toRemove.resource());
                System.out.println("Право удалено");
            } else {
                System.out.println("Неверный номер");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Поиск ролей"));

            List<String> searchOptions = Arrays.asList(
                    "По имени",
                    "По наличию права",
                    "По минимальному количеству прав"
            );

            String choice = ConsoleUtils.promptChoice(s, "Выберите тип поиска:", searchOptions);

            List<Role> roles = sys.getRoleManager().findAll();
            List<Role> result;

            switch (searchOptions.indexOf(choice)) {
                case 0: // По имени
                    String name = Objects.requireNonNull(ConsoleUtils.promptString(s, "Введите часть имени: ", true)).toLowerCase();
                    result = roles.stream()
                            .filter(r -> r.getName().toLowerCase().contains(name))
                            .toList();
                    sys.getAuditLog().log("ROLE_SEARCH", sys.getCurrentUser(), "system",
                            "Searched roles by name: " + name);
                    break;

                case 1: // По наличию права
                    String pname = ConsoleUtils.promptString(s, "Permission name: ", true);
                    String res = ConsoleUtils.promptString(s, "Resource: ", true);

                    result = roles.stream()
                            .filter(r -> r.hasPermission(pname, res))
                            .toList();
                    sys.getAuditLog().log("ROLE_SEARCH", sys.getCurrentUser(), "system",
                            "Searched roles by permission: " + pname + " on " + res);
                    break;

                case 2: // По минимальному количеству прав
                    int min = ConsoleUtils.promptInt(s, "Минимум прав: ", 0, 1000);

                    result = roles.stream()
                            .filter(r -> r.getPermissions().size() >= min)
                            .toList();
                    sys.getAuditLog().log("ROLE_SEARCH", sys.getCurrentUser(), "system",
                            "Searched roles with min permissions: " + min);
                    break;

                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            System.out.println(FormatUtils.formatHeader("Результаты поиска (" + result.size() + " найдено)"));
            String[] headers = {"Role Name", "Permissions", "Description"};
            List<String[]> rows = new ArrayList<>();
            for (Role r : result) {
                rows.add(new String[]{
                        r.getName(),
                        String.valueOf(r.getPermissions().size()),
                        FormatUtils.truncate(r.getDescription(), 40)
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        // =================== Назначения ===================
        parser.registerCommand("assign-role", "Назначить роль пользователю", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Назначение роли"));

            String uname = ConsoleUtils.promptString(s, "username: ", true);
            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("ASSIGN_ROLE_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();

            List<Role> allRoles = sys.getRoleManager().findAll();
            if (allRoles.isEmpty()) {
                System.out.println("Нет доступных ролей");
                return;
            }

            System.out.println(FormatUtils.formatHeader("Available Roles"));
            String[] roleHeaders = {"#", "Role Name", "Description", "Permissions"};
            List<String[]> roleRows = new ArrayList<>();
            for(int i = 0; i < allRoles.size(); i++) {
                Role r = allRoles.get(i);
                roleRows.add(new String[]{
                        String.valueOf(i + 1),
                        r.getName(),
                        FormatUtils.truncate(r.getDescription(), 30),
                        String.valueOf(r.getPermissions().size())
                });
            }
            System.out.println(FormatUtils.formatTable(roleHeaders, roleRows));

            System.out.print("Выберите номер роли: ");
            int roleIdx = Integer.parseInt(s.nextLine().trim()) - 1;
            if (roleIdx < 0 || roleIdx >= allRoles.size()) {
                System.out.println("Неверный номер");
                return;
            }
            Role role = allRoles.get(roleIdx);

            List<String> types = Arrays.asList("permanent", "temporary");
            String type = ConsoleUtils.promptChoice(s, "Выберите тип назначения:", types);

            String expires = null;
            if ("temporary".equals(type)) {
                expires = ConsoleUtils.promptString(s, "Дата окончания (yyyy-MM-dd): ", true);
            }

            String reason = ConsoleUtils.promptString(s, "Причина назначения: ", false);
            if (reason == null) reason = "";

            AssignmentMetadata meta = new AssignmentMetadata(
                    sys.getCurrentUser(),
                    java.time.LocalDate.now().toString(),
                    reason
            );

            TemporaryAssignment a = new TemporaryAssignment(u, role, meta);
            if (expires != null) a.expiresAt = expires;

            sys.getAssignmentManager().add(a);
            sys.getAuditLog().log("ASSIGN_ROLE", sys.getCurrentUser(), uname,
                    "Assigned role: " + role.getName() + " (" + type + "), reason: " + reason);
            System.out.println("Роль назначена");
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (s, sys) -> {
            String uname = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("REVOKE_ROLE_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);

            if (assignments.isEmpty()) {
                System.out.println("У пользователя нет назначенных ролей");
                return;
            }

            String[] assignHeaders = {"#", "Role", "Type", "Status", "Assigned At"};
            List<String[]> assignRows = new ArrayList<>();
            for(int i = 0; i < assignments.size(); i++) {
                RoleAssignment ra = assignments.get(i);
                assignRows.add(new String[]{
                        String.valueOf(i + 1),
                        ra.role().getName(),
                        ra.assignmentType(),
                        ra.isActive() ? "ACTIVE" : "EXPIRED",
                        ra.metadata().assignedAt()
                });
            }
            System.out.println(FormatUtils.formatTable(assignHeaders, assignRows));

            System.out.print("Выберите номер назначения для отзыва: ");
            int idx = Integer.parseInt(s.nextLine().trim()) - 1;

            if (idx < 0 || idx >= assignments.size()) {
                System.out.println("Неверный номер");
                return;
            }

            RoleAssignment toRevoke = assignments.get(idx);

            if (toRevoke instanceof TemporaryAssignment ta) {
                ta.expiresAt = java.time.LocalDate.now().minusDays(1).toString();
                sys.getAuditLog().log("REVOKE_ROLE", sys.getCurrentUser(), uname,
                        "Revoked role: " + ta.role().getName());
                System.out.println("Роль отозвана");
            } else {
                System.out.println("Невозможно отозвать это назначение");
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_LIST", sys.getCurrentUser(), "system", "Viewed all assignments");

            List<RoleAssignment> assignments = sys.getAssignmentManager().findAll();
            System.out.println(FormatUtils.formatHeader("Assignments List (" + assignments.size() + ")"));

            String[] headers = {"User", "Role", "Type", "Status", "Assigned At", "Assigned By"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment a : assignments) {
                rows.add(new String[]{
                        a.user().username(),
                        a.role().getName(),
                        a.assignmentType(),
                        a.isActive() ? "ACTIVE" : "EXPIRED",
                        a.metadata().assignedAt(),
                        a.metadata().assignedBy()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));

            long active = assignments.stream().filter(RoleAssignment::isActive).count();
            System.out.println("\n" + FormatUtils.formatBox("Statistics"));
            System.out.println("Total: " + assignments.size());
            System.out.println("Active: " + active);
            System.out.println("Expired: " + (assignments.size() - active));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-list-user", "Назначения пользователя", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(user);

            sys.getAuditLog().log("ASSIGNMENT_LIST_USER", sys.getCurrentUser(), username,
                    "Viewed assignments for user");

            System.out.println(FormatUtils.formatHeader("Назначения пользователя " + username));
            String[] headers = {"Role", "Type", "Status", "Assigned At", "Assigned By"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment a : assignments) {
                rows.add(new String[]{
                        a.role().getName(),
                        a.assignmentType(),
                        a.isActive() ? "ACTIVE" : "EXPIRED",
                        a.metadata().assignedAt(),
                        a.metadata().assignedBy()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-list-role", "Пользователи роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(role);

            sys.getAuditLog().log("ASSIGNMENT_LIST_ROLE", sys.getCurrentUser(), name,
                    "Viewed users for role");

            System.out.println(FormatUtils.formatHeader("Пользователи с ролью " + name));
            String[] headers = {"Username", "Full Name", "Email", "Status"};
            List<String[]> rows = new ArrayList<>();

            assignments.stream()
                    .map(RoleAssignment::user)
                    .distinct()
                    .forEach(u -> {
                        boolean isActive = assignments.stream()
                                .anyMatch(a -> a.user().equals(u) && a.isActive());
                        rows.add(new String[]{
                                u.username(),
                                FormatUtils.truncate(u.fullName(), 20),
                                FormatUtils.truncate(u.email(), 25),
                                isActive ? "ACTIVE" : "EXPIRED"
                        });
                    });
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-active", "Активные назначения", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_ACTIVE", sys.getCurrentUser(), "system", "Viewed active assignments");

            List<RoleAssignment> active = sys.getAssignmentManager()
                    .findAll()
                    .stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            System.out.println(FormatUtils.formatHeader("Active Assignments (" + active.size() + ")"));
            String[] headers = {"User", "Role", "Type", "Assigned At"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment a : active) {
                rows.add(new String[]{
                        a.user().username(),
                        a.role().getName(),
                        a.assignmentType(),
                        a.metadata().assignedAt()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-expired", "Истёкшие назначения", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_EXPIRED", sys.getCurrentUser(), "system", "Viewed expired assignments");

            List<RoleAssignment> expired = sys.getAssignmentManager()
                    .findAll()
                    .stream()
                    .filter(a -> !a.isActive())
                    .toList();

            System.out.println(FormatUtils.formatHeader("Expired Assignments (" + expired.size() + ")"));
            String[] headers = {"User", "Role", "Type", "Assigned At"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment a : expired) {
                rows.add(new String[]{
                        a.user().username(),
                        a.role().getName(),
                        a.assignmentType(),
                        a.metadata().assignedAt()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (s, sys) -> {
            String aid = ConsoleUtils.promptString(s, "Assignment ID: ", true);

            Optional<RoleAssignment> raOpt = sys.getAssignmentManager().findById(aid);
            if (raOpt.isEmpty()) {
                System.out.println("Назначение не найдено");
                return;
            }

            RoleAssignment ra = raOpt.get();
            if (!(ra instanceof TemporaryAssignment a)) {
                System.out.println("Это не временное назначение, продление невозможно");
                sys.getAuditLog().log("ASSIGNMENT_EXTEND_FAILED", sys.getCurrentUser(), aid,
                        "Not a temporary assignment");
                return;
            }

            String d = ConsoleUtils.promptString(s, "Новая дата окончания (yyyy-MM-dd): ", true);
            a.extend(d);
            sys.getAuditLog().log("ASSIGNMENT_EXTEND", sys.getCurrentUser(), aid,
                    "Extended to: " + d);
            System.out.println("Продлено");
        });

        parser.registerCommand("assignment-search", "Поиск назначений", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Поиск назначений"));

            List<String> searchOptions = Arrays.asList(
                    "По пользователю",
                    "По роли",
                    "По типу",
                    "По статусу"
            );

            String choice = ConsoleUtils.promptChoice(s, "Выберите тип поиска:", searchOptions);
            List<RoleAssignment> list = sys.getAssignmentManager().findAll();
            List<RoleAssignment> results = new ArrayList<>();

            switch (searchOptions.indexOf(choice)) {
                case 0: // По пользователю
                    String uname = ConsoleUtils.promptString(s, "username: ", true);
                    results = list.stream()
                            .filter(a -> a.user().username().equals(uname))
                            .toList();
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by user: " + uname);
                    break;

                case 1: // По роли
                    String role = ConsoleUtils.promptString(s, "role: ", true);
                    results = list.stream()
                            .filter(a -> a.role().getName().equals(role))
                            .toList();
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by role: " + role);
                    break;

                case 2: // По типу
                    List<String> types = Arrays.asList("PERMANENT", "TEMPORARY");
                    String type = ConsoleUtils.promptChoice(s, "Выберите тип:", types);
                    results = list.stream()
                            .filter(a -> a.assignmentType().equalsIgnoreCase(type))
                            .toList();
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by type: " + type);
                    break;

                case 3: // По статусу
                    boolean active = ConsoleUtils.promptYesNo(s, "Показать активные?");
                    results = list.stream()
                            .filter(a -> a.isActive() == active)
                            .toList();
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by active status: " + active);
                    break;
            }

            System.out.println(FormatUtils.formatHeader("Результаты поиска (" + results.size() + ")"));
            String[] headers = {"User", "Role", "Type", "Status", "Assigned At"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment a : results) {
                rows.add(new String[]{
                        a.user().username(),
                        a.role().getName(),
                        a.assignmentType(),
                        a.isActive() ? "ACTIVE" : "EXPIRED",
                        a.metadata().assignedAt()
                });
            }
            System.out.println(FormatUtils.formatTable(headers, rows));
            ConsoleUtils.pause(s);
        });

        // =================== Права ===================
        parser.registerCommand("permissions-user", "Все права пользователя", (s, sys) -> {
            String uname = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("PERMISSIONS_USER_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();
            Map<String, Set<String>> permsByResource = new HashMap<>();

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);
            for (RoleAssignment a : assignments) {
                if (a.isActive()) {
                    for (Permission p : a.role().getPermissions()) {
                        permsByResource.computeIfAbsent(p.resource(), k -> new HashSet<>()).add(p.name());
                    }
                }
            }

            sys.getAuditLog().log("PERMISSIONS_USER", sys.getCurrentUser(), uname, "Viewed user permissions");

            System.out.println(FormatUtils.formatBox("Права пользователя " + uname));
            if (permsByResource.isEmpty()) {
                System.out.println("  Нет прав");
            } else {
                String[] headers = {"Resource", "Permissions"};
                List<String[]> rows = new ArrayList<>();
                for (Map.Entry<String, Set<String>> entry : permsByResource.entrySet()) {
                    rows.add(new String[]{
                            entry.getKey(),
                            String.join(", ", entry.getValue())
                    });
                }
                System.out.println(FormatUtils.formatTable(headers, rows));
            }
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("permissions-check", "Проверить право пользователя", (s, sys) -> {
            String uname = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                sys.getAuditLog().log("PERMISSIONS_CHECK_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();

            String pname = ConsoleUtils.promptString(s, "Permission name: ", true);
            String res = ConsoleUtils.promptString(s, "Resource: ", true);

            Optional<RoleAssignment> a = sys.getAssignmentManager().findByUser(u).stream()
                    .filter(as -> as.role().hasPermission(pname, res)).findFirst();

            if (a.isPresent()) {
                System.out.println("Есть право через роль: " + a.get().role().getName());
                sys.getAuditLog().log("PERMISSIONS_CHECK", sys.getCurrentUser(), uname,
                        "Checked permission: " + pname + " on " + res + " - GRANTED via " + a.get().role().getName());
            } else {
                System.out.println("Нет права");
                sys.getAuditLog().log("PERMISSIONS_CHECK", sys.getCurrentUser(), uname,
                        "Checked permission: " + pname + " on " + res + " - DENIED");
            }
        });

        // =================== Audit команды ===================
        parser.registerCommand("audit-log", "Просмотр журнала аудита", (s, sys) -> {
            System.out.println(FormatUtils.formatHeader("Audit Log Options"));

            List<String> auditOptions = Arrays.asList(
                    "Показать все записи",
                    "Поиск по исполнителю",
                    "Поиск по действию",
                    "Сохранить в файл"
            );

            String choice = ConsoleUtils.promptChoice(s, "Выберите действие:", auditOptions);

            switch (auditOptions.indexOf(choice)) {
                case 0:
                    List<AuditLog.AuditEntry> all = sys.getAuditLog().getAll();
                    System.out.println(FormatUtils.formatHeader("Audit Log (" + all.size() + " entries)"));

                    String[] headers = {"Timestamp", "Action", "Performer", "Target", "Details"};
                    List<String[]> rows = new ArrayList<>();

                    for (AuditLog.AuditEntry e : all) {
                        rows.add(new String[]{
                                e.timestamp(),
                                FormatUtils.truncate(e.action(), 15),
                                e.performer(),
                                FormatUtils.truncate(e.target(), 15),
                                FormatUtils.truncate(e.details(), 30)
                        });
                    }
                    System.out.println(FormatUtils.formatTable(headers, rows));

                    sys.getAuditLog().log("AUDIT_VIEW", sys.getCurrentUser(), "system",
                            "Viewed all audit entries");
                    break;

                case 1:
                    String performer = ConsoleUtils.promptString(s, "Введите имя исполнителя: ", true);
                    List<AuditLog.AuditEntry> byPerformer = sys.getAuditLog().getByPerformer(performer);

                    System.out.println(FormatUtils.formatHeader("Audit entries for performer: " + performer));
                    String[] perfHeaders = {"Timestamp", "Action", "Target", "Details"};
                    List<String[]> perfRows = new ArrayList<>();

                    for (AuditLog.AuditEntry e : byPerformer) {
                        perfRows.add(new String[]{
                                e.timestamp(),
                                FormatUtils.truncate(e.action(), 15),
                                FormatUtils.truncate(e.target(), 15),
                                FormatUtils.truncate(e.details(), 40)
                        });
                    }
                    System.out.println(FormatUtils.formatTable(perfHeaders, perfRows));
                    System.out.println("Total: " + byPerformer.size());

                    sys.getAuditLog().log("AUDIT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched audit by performer: " + performer);
                    break;

                case 2:
                    String action = ConsoleUtils.promptString(s, "Введите действие: ", true);
                    List<AuditLog.AuditEntry> byAction = sys.getAuditLog().getByAction(action);

                    System.out.println(FormatUtils.formatHeader("Audit entries for action: " + action));
                    String[] actHeaders = {"Timestamp", "Performer", "Target", "Details"};
                    List<String[]> actRows = new ArrayList<>();

                    for (AuditLog.AuditEntry e : byAction) {
                        actRows.add(new String[]{
                                e.timestamp(),
                                e.performer(),
                                FormatUtils.truncate(e.target(), 15),
                                FormatUtils.truncate(e.details(), 40)
                        });
                    }
                    System.out.println(FormatUtils.formatTable(actHeaders, actRows));
                    System.out.println("Total: " + byAction.size());

                    sys.getAuditLog().log("AUDIT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched audit by action: " + action);
                    break;

                case 3:
                    String filename = ConsoleUtils.promptString(s, "Имя файла (например, audit.log): ", true);
                    sys.getAuditLog().saveToFile(filename);
                    sys.getAuditLog().log("AUDIT_SAVE", sys.getCurrentUser(), filename,
                            "Saved audit log to file");
                    break;
            }
            ConsoleUtils.pause(s);
        });

        // =================== Reports ===================
        parser.registerCommand("report-users", "Отчёт по всем пользователям с их ролями", (s, sys) -> {
            ReportGenerator reportGen = new ReportGenerator();
            String report = reportGen.generateUserReport(sys.getUserManager(), sys.getAssignmentManager());

            System.out.println("\n" + report);
            sys.getAuditLog().log("REPORT_USERS", sys.getCurrentUser(), "system", "Generated user report");

            boolean save = ConsoleUtils.promptYesNo(s, "Сохранить отчёт в файл?");
            if (save) {
                String filename = ConsoleUtils.promptString(s, "Имя файла (например, user_report.txt): ", true);
                reportGen.exportToFile(report, filename);
                sys.getAuditLog().log("REPORT_USERS_SAVE", sys.getCurrentUser(), filename,
                        "Saved user report to file");
            }
        });

        parser.registerCommand("report-roles", "Отчёт по ролям с количеством пользователей", (s, sys) -> {
            ReportGenerator reportGen = new ReportGenerator();
            String report = reportGen.generateRoleReport(sys.getRoleManager(), sys.getAssignmentManager());

            System.out.println("\n" + report);
            sys.getAuditLog().log("REPORT_ROLES", sys.getCurrentUser(), "system", "Generated role report");

            boolean save = ConsoleUtils.promptYesNo(s, "Сохранить отчёт в файл?");
            if (save) {
                String filename = ConsoleUtils.promptString(s, "Имя файла (например, role_report.txt): ", true);
                reportGen.exportToFile(report, filename);
                sys.getAuditLog().log("REPORT_ROLES_SAVE", sys.getCurrentUser(), filename,
                        "Saved role report to file");
            }
        });

        parser.registerCommand("report-matrix", "Матрица прав (пользователи × ресурсы)", (s, sys) -> {
            ReportGenerator reportGen = new ReportGenerator();
            String report = reportGen.generatePermissionMatrix(sys.getUserManager(), sys.getAssignmentManager());

            System.out.println("\n" + report);
            sys.getAuditLog().log("REPORT_MATRIX", sys.getCurrentUser(), "system", "Generated permission matrix");

            boolean save = ConsoleUtils.promptYesNo(s, "Сохранить отчёт в файл?");
            if (save) {
                String filename = ConsoleUtils.promptString(s, "Имя файла (например, matrix_report.txt): ", true);
                reportGen.exportToFile(report, filename);
                sys.getAuditLog().log("REPORT_MATRIX_SAVE", sys.getCurrentUser(), filename,
                        "Saved permission matrix to file");
            }
        });

        // =================== Служебные ===================
        parser.registerCommand("help", "Справка по командам", (s, sys) -> {
            parser.printHelp();
            sys.getAuditLog().log("HELP", sys.getCurrentUser(), "system", "Viewed help");
        });

        parser.registerCommand("stats", "Статистика системы", (s, sys) -> {
            System.out.println(sys.generateStatistics());
            sys.getAuditLog().log("STATS", sys.getCurrentUser(), "system", "Viewed system statistics");
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("clear", "Очистка экрана", (s, sys) -> {
            ConsoleUtils.clearScreen();
            sys.getAuditLog().log("CLEAR", sys.getCurrentUser(), "system", "Cleared screen");
        });

        parser.registerCommand("exit", "Выход", (s, sys) -> {
            boolean confirm = ConsoleUtils.promptYesNo(s, "Подтвердить выход");
            if (confirm) {
                sys.getAuditLog().log("EXIT", sys.getCurrentUser(), "system", "User exited the system");
                System.out.println("Выход");
                System.exit(0);
            } else {
                sys.getAuditLog().log("EXIT_CANCELLED", sys.getCurrentUser(), "system", "Exit cancelled");
                System.out.println("Возврат в систему");
            }
        });
    }
}