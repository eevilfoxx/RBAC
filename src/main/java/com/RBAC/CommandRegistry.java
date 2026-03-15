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
            ConsoleUtils.printHeader("=== Users ===");
            sys.getUserManager().findAll().forEach(u ->
                    System.out.printf("%-15s | %-20s | %-30s%n", u.username(), u.fullName(), u.email()));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (s, sys) -> {
            ConsoleUtils.printHeader("Создание нового пользователя");

            String username = ConsoleUtils.promptString(s, "username: ", true);
            String fullName = ConsoleUtils.promptString(s, "fullName: ", true);
            String email = ConsoleUtils.promptString(s, "email: ", true);

            User user = new User(username, fullName, email);
            try {
                sys.getUserManager().add(user);
                sys.getAuditLog().log("USER_CREATE", sys.getCurrentUser(), username,
                        "User created: " + fullName + ", " + email);
                ConsoleUtils.printSuccess("Пользователь создан");
            }
            catch(Exception e){
                sys.getAuditLog().log("USER_CREATE_ERROR", sys.getCurrentUser(), username,
                        "Error: " + e.getMessage());
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("USER_VIEW_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }

            User u = userOpt.get();
            sys.getAuditLog().log("USER_VIEW", sys.getCurrentUser(), username, "Viewed user details");

            ConsoleUtils.printHeader("Информация о пользователе");
            System.out.println("User: " + u.username() + " / " + u.fullName() + " / " + u.email());
            System.out.println("Roles & Permissions:");

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);
            if (assignments.isEmpty()) {
                System.out.println("  Нет назначенных ролей");
            } else {
                for(RoleAssignment a : assignments){
                    System.out.println(" - Role: " + a.role().getName() + " (" + a.assignmentType() + ")");
                    a.role().getPermissions().forEach(p -> System.out.println("    * " + p.name() + " on " + p.resource()));
                }
            }
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("USER_UPDATE_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }

            User u = userOpt.get();
            ConsoleUtils.printInfo("Текущие данные: " + u.fullName() + " / " + u.email());

            String fn = ConsoleUtils.promptString(s, "Новое fullName (Enter - оставить): ", false);
            String email = ConsoleUtils.promptString(s, "Новый email (Enter - оставить): ", false);

            if (fn == null) fn = u.fullName();
            if (email == null) email = u.email();

            sys.getUserManager().update(username, fn, email);
            sys.getAuditLog().log("USER_UPDATE", sys.getCurrentUser(), username,
                    "Updated: fullName=" + fn + ", email=" + email);
            ConsoleUtils.printSuccess("Данные обновлены");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("USER_DELETE_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }

            User u = userOpt.get();
            ConsoleUtils.printWarning("Внимание: будет удалён пользователь " + u.username() + " (" + u.fullName() + ")");

            boolean confirm = ConsoleUtils.promptYesNo(s, "Подтвердите удаление");
            if (!confirm) {
                ConsoleUtils.printInfo("Удаление отменено");
                sys.getAuditLog().log("USER_DELETE_CANCELLED", sys.getCurrentUser(), username, "Deletion cancelled");
                return;
            }

            sys.getUserManager().remove(u);
            sys.getAuditLog().log("USER_DELETE", sys.getCurrentUser(), username,
                    "User deleted: " + u.fullName() + ", " + u.email());
            ConsoleUtils.printSuccess("Пользователь удалён");
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (s, sys) -> {
            ConsoleUtils.printHeader("Поиск пользователей");

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
                    ConsoleUtils.printError("Неверный выбор");
                    sys.getAuditLog().log("USER_SEARCH_FAILED", sys.getCurrentUser(), "system", "Invalid search option");
                    return;
            }

            sys.getAuditLog().log("USER_SEARCH", sys.getCurrentUser(), "system",
                    "Searched users with filter: " + filter);

            ConsoleUtils.printHeader("Результаты поиска (" + results.size() + " найдено)");
            results.forEach(u -> System.out.printf("%-15s | %-20s | %-30s%n",
                    u.username(), u.fullName(), u.email()));
            ConsoleUtils.pause(s);
        });

        // =================== Роли ===================
        parser.registerCommand("role-list", "Список всех ролей", (s, sys) -> {
            sys.getAuditLog().log("ROLE_LIST", sys.getCurrentUser(), "system", "Viewed all roles");
            ConsoleUtils.printHeader("=== Roles ===");
            sys.getRoleManager().findAll().forEach(r ->
                    System.out.printf("%-15s | Permissions: %d | ID: %s%n",
                            r.getName(), r.getPermissions().size(), r.getId()));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("role-create", "Создать роль", (s, sys) -> {
            ConsoleUtils.printHeader("Создание новой роли");

            String name = ConsoleUtils.promptString(s, "Название роли: ", true);
            String desc = ConsoleUtils.promptString(s, "Описание: ", false);
            if (desc == null) desc = "";

            Role r = new Role(name, desc);
            sys.getRoleManager().add(r);
            sys.getAuditLog().log("ROLE_CREATE", sys.getCurrentUser(), name,
                    "Created role with description: " + desc);
            ConsoleUtils.printSuccess("Роль создана");

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
                ConsoleUtils.printSuccess("Право добавлено");

                addMore = ConsoleUtils.promptYesNo(s, "Добавить ещё право?");
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                sys.getAuditLog().log("ROLE_VIEW_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();
            sys.getAuditLog().log("ROLE_VIEW", sys.getCurrentUser(), name, "Viewed role details");
            System.out.println(r.format());
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("role-update", "Обновить роль (название/описание)", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
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
                ConsoleUtils.printSuccess("Роль обновлена");
            } else {
                ConsoleUtils.printInfo("Изменений не внесено");
            }
        });

        parser.registerCommand("role-delete", "Удалить роль", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                sys.getAuditLog().log("ROLE_DELETE_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(r);

            if (!assignments.isEmpty()) {
                ConsoleUtils.printWarning("Роль назначена пользователям:");
                assignments.stream()
                        .map(RoleAssignment::user)
                        .distinct()
                        .forEach(u -> System.out.println(" - " + u.username()));
            }

            boolean confirm = ConsoleUtils.promptYesNo(s, "Подтвердить удаление");
            if (!confirm) {
                ConsoleUtils.printInfo("Удаление отменено");
                sys.getAuditLog().log("ROLE_DELETE_CANCELLED", sys.getCurrentUser(), name, "Deletion cancelled");
                return;
            }

            sys.getRoleManager().remove(r);
            sys.getAuditLog().log("ROLE_DELETE", sys.getCurrentUser(), name,
                    "Deleted role with " + r.getPermissions().size() + " permissions");
            ConsoleUtils.printSuccess("Роль удалена");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
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
            ConsoleUtils.printSuccess("Право добавлено");
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                sys.getAuditLog().log("PERMISSION_REMOVE_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }

            Role r = roleOpt.get();
            List<Permission> perms = new ArrayList<>(r.getPermissions());

            if (perms.isEmpty()) {
                ConsoleUtils.printWarning("У роли нет прав для удаления");
                return;
            }

            Permission toRemove = ConsoleUtils.promptChoice(s, "Выберите право для удаления:", perms);
            if (toRemove != null) {
                r.removePermission(toRemove);
                sys.getAuditLog().log("PERMISSION_REMOVE", sys.getCurrentUser(), name,
                        "Removed permission: " + toRemove.name() + " on " + toRemove.resource());
                ConsoleUtils.printSuccess("Право удалено");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей", (s, sys) -> {
            ConsoleUtils.printHeader("Поиск ролей");

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
                    ConsoleUtils.printError("Неверный выбор");
                    return;
            }

            ConsoleUtils.printHeader("Результаты поиска (" + result.size() + " найдено)");
            result.forEach(r ->
                    System.out.println(r.getName() + " | permissions: " + r.getPermissions().size()));
            ConsoleUtils.pause(s);
        });

        // =================== Назначения ===================
        parser.registerCommand("assign-role", "Назначить роль пользователю", (s, sys) -> {
            ConsoleUtils.printHeader("Назначение роли");

            String uname = ConsoleUtils.promptString(s, "username: ", true);
            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("ASSIGN_ROLE_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();

            List<Role> allRoles = sys.getRoleManager().findAll();
            if (allRoles.isEmpty()) {
                ConsoleUtils.printWarning("Нет доступных ролей");
                return;
            }

            Role role = ConsoleUtils.promptChoice(s, "Выберите роль:", allRoles);

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
            assert role != null;
            sys.getAuditLog().log("ASSIGN_ROLE", sys.getCurrentUser(), uname,
                    "Assigned role: " + role.getName() + " (" + type + "), reason: " + reason);
            ConsoleUtils.printSuccess("Роль назначена");
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (s, sys) -> {
            String uname = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("REVOKE_ROLE_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);

            if (assignments.isEmpty()) {
                ConsoleUtils.printWarning("У пользователя нет назначенных ролей");
                return;
            }

            RoleAssignment toRevoke = ConsoleUtils.promptChoice(s, "Выберите назначение для отзыва:", assignments);

            if (toRevoke instanceof TemporaryAssignment ta) {
                ta.expiresAt = java.time.LocalDate.now().minusDays(1).toString();
                sys.getAuditLog().log("REVOKE_ROLE", sys.getCurrentUser(), uname,
                        "Revoked role: " + ta.role().getName());
                ConsoleUtils.printSuccess("Роль отозвана");
            } else {
                ConsoleUtils.printError("Невозможно отозвать это назначение");
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_LIST", sys.getCurrentUser(), "system", "Viewed all assignments");
            ConsoleUtils.printHeader("=== Assignments ===");
            sys.getAssignmentManager().findAll().forEach(a ->
                    System.out.printf("%-15s | %-15s | %-10s | %-10s | %s%n",
                            a.user().username(), a.role().getName(),
                            a.assignmentType(), a.isActive() ? "active" : "expired",
                            a.metadata().assignedAt())
            );
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-list-user", "Назначения пользователя", (s, sys) -> {
            String username = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(user);

            sys.getAuditLog().log("ASSIGNMENT_LIST_USER", sys.getCurrentUser(), username,
                    "Viewed assignments for user");

            ConsoleUtils.printHeader("Назначения пользователя " + username);
            assignments.forEach(a -> System.out.printf(
                    "%s | %s | %s | %s%n",
                    a.role().getName(),
                    a.assignmentType(),
                    a.isActive() ? "active" : "inactive",
                    a.metadata().assignedAt()
            ));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-list-role", "Пользователи роли", (s, sys) -> {
            String name = ConsoleUtils.promptString(s, "Имя роли: ", true);

            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(role);

            sys.getAuditLog().log("ASSIGNMENT_LIST_ROLE", sys.getCurrentUser(), name,
                    "Viewed users for role");

            ConsoleUtils.printHeader("Пользователи с ролью " + name);
            assignments.stream()
                    .map(RoleAssignment::user)
                    .distinct()
                    .forEach(u -> System.out.println(u.username()));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-active", "Активные назначения", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_ACTIVE", sys.getCurrentUser(), "system", "Viewed active assignments");
            ConsoleUtils.printHeader("Активные назначения");
            sys.getAssignmentManager()
                    .findAll()
                    .stream()
                    .filter(RoleAssignment::isActive)
                    .forEach(a -> System.out.printf(
                            "%s | %s | %s%n",
                            a.user().username(),
                            a.role().getName(),
                            a.assignmentType()
                    ));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-expired", "Истёкшие назначения", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_EXPIRED", sys.getCurrentUser(), "system", "Viewed expired assignments");
            ConsoleUtils.printHeader("Истёкшие назначения");
            sys.getAssignmentManager()
                    .findAll()
                    .stream()
                    .filter(a -> !a.isActive())
                    .forEach(a -> System.out.printf(
                            "%s | %s%n",
                            a.user().username(),
                            a.role().getName()
                    ));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (s, sys) -> {
            String aid = ConsoleUtils.promptString(s, "Assignment ID: ", true);

            Optional<RoleAssignment> raOpt = sys.getAssignmentManager().findById(aid);
            if (raOpt.isEmpty()) {
                ConsoleUtils.printError("Назначение не найдено");
                return;
            }

            RoleAssignment ra = raOpt.get();
            if (!(ra instanceof TemporaryAssignment a)) {
                ConsoleUtils.printError("Это не временное назначение, продление невозможно");
                sys.getAuditLog().log("ASSIGNMENT_EXTEND_FAILED", sys.getCurrentUser(), aid,
                        "Not a temporary assignment");
                return;
            }

            String d = ConsoleUtils.promptString(s, "Новая дата окончания (yyyy-MM-dd): ", true);
            a.extend(d);
            sys.getAuditLog().log("ASSIGNMENT_EXTEND", sys.getCurrentUser(), aid,
                    "Extended to: " + d);
            ConsoleUtils.printSuccess("Продлено");
        });

        parser.registerCommand("assignment-search", "Поиск назначений", (s, sys) -> {
            ConsoleUtils.printHeader("Поиск назначений");

            List<String> searchOptions = Arrays.asList(
                    "По пользователю",
                    "По роли",
                    "По типу",
                    "По статусу"
            );

            String choice = ConsoleUtils.promptChoice(s, "Выберите тип поиска:", searchOptions);
            List<RoleAssignment> list = sys.getAssignmentManager().findAll();

            switch (searchOptions.indexOf(choice)) {
                case 0: // По пользователю
                    String uname = ConsoleUtils.promptString(s, "username: ", true);
                    list.stream()
                            .filter(a -> a.user().username().equals(uname))
                            .forEach(a -> System.out.println(a.role().getName()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by user: " + uname);
                    break;

                case 1: // По роли
                    String role = ConsoleUtils.promptString(s, "role: ", true);
                    list.stream()
                            .filter(a -> a.role().getName().equals(role))
                            .forEach(a -> System.out.println(a.user().username()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by role: " + role);
                    break;

                case 2: // По типу
                    List<String> types = Arrays.asList("PERMANENT", "TEMPORARY");
                    String type = ConsoleUtils.promptChoice(s, "Выберите тип:", types);
                    list.stream()
                            .filter(a -> a.assignmentType().equalsIgnoreCase(type))
                            .forEach(a -> System.out.println(a.user().username()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by type: " + type);
                    break;

                case 3: // По статусу
                    boolean active = ConsoleUtils.promptYesNo(s, "Показать активные?");
                    list.stream()
                            .filter(a -> a.isActive() == active)
                            .forEach(a -> System.out.println(a.user().username()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched assignments by active status: " + active);
                    break;
            }
            ConsoleUtils.pause(s);
        });

        // =================== Права ===================
        parser.registerCommand("permissions-user", "Все права пользователя", (s, sys) -> {
            String uname = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("PERMISSIONS_USER_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();
            Map<String, List<String>> permsByResource = new HashMap<>();
            sys.getAssignmentManager().findByUser(u).forEach(a ->
                    a.role().getPermissions().forEach(p ->
                            permsByResource.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p.name())));

            sys.getAuditLog().log("PERMISSIONS_USER", sys.getCurrentUser(), uname, "Viewed user permissions");

            ConsoleUtils.printHeader("Права пользователя " + uname);
            permsByResource.forEach((res, list) ->
                    System.out.println(res + ": " + String.join(", ", list)));
            ConsoleUtils.pause(s);
        });

        parser.registerCommand("permissions-check", "Проверить право пользователя", (s, sys) -> {
            String uname = ConsoleUtils.promptString(s, "username: ", true);

            Optional<User> userOpt = sys.getUserManager().findByUsername(uname);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                sys.getAuditLog().log("PERMISSIONS_CHECK_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }

            User u = userOpt.get();

            String pname = ConsoleUtils.promptString(s, "Permission name: ", true);
            String res = ConsoleUtils.promptString(s, "Resource: ", true);

            Optional<RoleAssignment> a = sys.getAssignmentManager().findByUser(u).stream()
                    .filter(as -> as.role().hasPermission(pname, res)).findFirst();

            if (a.isPresent()) {
                ConsoleUtils.printSuccess("Есть право через роль: " + a.get().role().getName());
                sys.getAuditLog().log("PERMISSIONS_CHECK", sys.getCurrentUser(), uname,
                        "Checked permission: " + pname + " on " + res + " - GRANTED via " + a.get().role().getName());
            } else {
                ConsoleUtils.printError("Нет права");
                sys.getAuditLog().log("PERMISSIONS_CHECK", sys.getCurrentUser(), uname,
                        "Checked permission: " + pname + " on " + res + " - DENIED");
            }
        });

        // =================== Audit команды ===================
        parser.registerCommand("audit-log", "Просмотр журнала аудита", (s, sys) -> {
            ConsoleUtils.printHeader("Audit Log Options");

            List<String> auditOptions = Arrays.asList(
                    "Показать все записи",
                    "Поиск по исполнителю",
                    "Поиск по действию",
                    "Сохранить в файл"
            );

            String choice = ConsoleUtils.promptChoice(s, "Выберите действие:", auditOptions);

            switch (auditOptions.indexOf(choice)) {
                case 0:
                    sys.getAuditLog().printLog();
                    sys.getAuditLog().log("AUDIT_VIEW", sys.getCurrentUser(), "system",
                            "Viewed all audit entries");
                    break;

                case 1:
                    String performer = ConsoleUtils.promptString(s, "Введите имя исполнителя: ", true);
                    List<AuditLog.AuditEntry> byPerformer = sys.getAuditLog().getByPerformer(performer);
                    ConsoleUtils.printHeader("Audit entries for performer: " + performer);
                    byPerformer.forEach(e -> System.out.printf("%s | %s | %s | %s%n",
                            e.timestamp(), e.action(), e.target(), e.details()));
                    System.out.println("Total: " + byPerformer.size());
                    sys.getAuditLog().log("AUDIT_SEARCH", sys.getCurrentUser(), "system",
                            "Searched audit by performer: " + performer);
                    break;

                case 2:
                    String action = ConsoleUtils.promptString(s, "Введите действие: ", true);
                    List<AuditLog.AuditEntry> byAction = sys.getAuditLog().getByAction(action);
                    ConsoleUtils.printHeader("Audit entries for action: " + action);
                    byAction.forEach(e -> System.out.printf("%s | %s | %s | %s%n",
                            e.timestamp(), e.performer(), e.target(), e.details()));
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
                ConsoleUtils.printInfo("Выход");
                System.exit(0);
            } else {
                sys.getAuditLog().log("EXIT_CANCELLED", sys.getCurrentUser(), "system", "Exit cancelled");
                ConsoleUtils.printInfo("Возврат в систему");
            }
        });
    }
}