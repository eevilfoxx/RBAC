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
            System.out.println("=== Users ===");
            sys.getUserManager().findAll().forEach(u ->
                    System.out.printf("%-15s | %-20s | %-30s%n", u.username(), u.fullName(), u.email()));
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            System.out.print("fullName: "); String fullName = s.nextLine().trim();
            System.out.print("email: "); String email = s.nextLine().trim();
            if(username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                System.out.println("Ошибка: пустые данные");
                sys.getAuditLog().log("USER_CREATE_FAILED", sys.getCurrentUser(), username, "Empty data provided");
                return;
            }
            User user = new User(username, fullName, email);
            try {
                sys.getUserManager().add(user);
                sys.getAuditLog().log("USER_CREATE", sys.getCurrentUser(), username, "User created: " + fullName + ", " + email);
                System.out.println("Пользователь создан");
            }
            catch(Exception e){
                sys.getAuditLog().log("USER_CREATE_ERROR", sys.getCurrentUser(), username, "Error: " + e.getMessage());
                System.out.println("Ошибка: "+e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("USER_VIEW_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }
            sys.getAuditLog().log("USER_VIEW", sys.getCurrentUser(), username, "Viewed user details");
            System.out.println("User: "+u.username()+" / "+u.fullName()+" / "+u.email());
            System.out.println("Roles & Permissions:");
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);

            for(RoleAssignment a : assignments){
                System.out.println(" - Role: "+a.role().getName()+" ("+a.assignmentType()+")");
                a.role().getPermissions().forEach(p -> System.out.println("    * "+p.name()+" on "+p.resource()));
            }
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("USER_UPDATE_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }
            System.out.print("fullName: "); String fn = s.nextLine().trim();
            System.out.print("email: "); String email = s.nextLine().trim();
            sys.getUserManager().update(username, fn, email);
            sys.getAuditLog().log("USER_UPDATE", sys.getCurrentUser(), username, "Updated: fullName=" + fn + ", email=" + email);
            System.out.println("Данные обновлены");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("USER_DELETE_FAILED", sys.getCurrentUser(), username, "User not found");
                return;
            }
            System.out.print("Подтвердите удаление (да): "); String c = s.nextLine().trim();
            if(!"да".equalsIgnoreCase(c)){
                System.out.println("Отмена");
                sys.getAuditLog().log("USER_DELETE_CANCELLED", sys.getCurrentUser(), username, "Deletion cancelled");
                return;
            }
            sys.getUserManager().remove(u);
            sys.getAuditLog().log("USER_DELETE", sys.getCurrentUser(), username, "User deleted: " + u.fullName() + ", " + u.email());
            System.out.println("Пользователь удалён");
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (s, sys) -> {
            System.out.println("1. По username\n2. По email\n3. По домену email\n4. По полному имени");
            System.out.print("Выбор: "); String choice = s.nextLine().trim();
            System.out.print("Введите фильтр: "); String filter = s.nextLine().trim().toLowerCase();
            List<User> results;
            switch(choice){
                case "1": results = sys.getUserManager().findByFilter(u->u.username().toLowerCase().contains(filter)); break;
                case "2": results = sys.getUserManager().findByFilter(u->u.email().toLowerCase().contains(filter)); break;
                case "3": results = sys.getUserManager().findByFilter(u->u.email().toLowerCase().endsWith(filter)); break;
                case "4": results = sys.getUserManager().findByFilter(u->u.fullName().toLowerCase().contains(filter)); break;
                default:
                    System.out.println("Неверный выбор");
                    sys.getAuditLog().log("USER_SEARCH_FAILED", sys.getCurrentUser(), "system", "Invalid search option: " + choice);
                    return;
            }
            sys.getAuditLog().log("USER_SEARCH", sys.getCurrentUser(), "system", "Searched users with filter: " + filter + " (option " + choice + ")");
            System.out.println("=== Results ===");
            results.forEach(u -> System.out.printf("%-15s | %-20s | %-30s%n", u.username(), u.fullName(), u.email()));
        });

        // =================== Роли ===================
        parser.registerCommand("role-list", "Список всех ролей", (s, sys) -> {
            sys.getAuditLog().log("ROLE_LIST", sys.getCurrentUser(), "system", "Viewed all roles");
            System.out.println("=== Roles ===");
            sys.getRoleManager().findAll().forEach(r ->
                    System.out.printf("%-15s | Permissions: %d | ID: %s%n", r.getName(), r.getPermissions().size(), r.getId()));
        });

        parser.registerCommand("role-create", "Создать роль", (s, sys) -> {
            System.out.print("Название роли: "); String name = s.nextLine().trim();
            System.out.print("Описание: "); String desc = s.nextLine().trim();
            Role r = new Role(name, desc);
            sys.getRoleManager().add(r);
            sys.getAuditLog().log("ROLE_CREATE", sys.getCurrentUser(), name, "Created role with description: " + desc);
            System.out.println("Роль создана");
            while(true){
                System.out.print("Добавить право? (да/нет): "); String ans = s.nextLine().trim();
                if(!"да".equalsIgnoreCase(ans)) break;
                System.out.print("Name: "); String p_name = s.nextLine().trim();
                System.out.print("Resource: "); String res = s.nextLine().trim();
                System.out.print("Description: "); String p_desc = s.nextLine().trim();
                Permission p = new Permission(p_name, res, p_desc);
                r.addPermission(p);
                sys.getAuditLog().log("PERMISSION_ADD", sys.getCurrentUser(), name, "Added permission: " + p_name + " on " + res);
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));

            if(r==null){
                System.out.println("Не найдена");
                sys.getAuditLog().log("ROLE_VIEW_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }
            sys.getAuditLog().log("ROLE_VIEW", sys.getCurrentUser(), name, "Viewed role details");
            System.out.println(r.format());
        });

        parser.registerCommand("role-update", "Обновить роль (название/описание)", (s, sys) -> {
            System.out.print("Имя роли: ");
            String name = s.nextLine().trim();

            Role role = sys.getRoleManager()
                    .findByName(name)
                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));

            System.out.print("Новое имя (Enter чтобы оставить): ");
            String newName = s.nextLine().trim();

            System.out.print("Новое описание (Enter чтобы оставить): ");
            String newDesc = s.nextLine().trim();

            if(!newName.isEmpty() && !newDesc.isEmpty()) {
                sys.getRoleManager().remove(role);
                Role new_role = new Role(newName,newDesc);
                sys.getRoleManager().add(new_role);
                sys.getAuditLog().log("ROLE_UPDATE", sys.getCurrentUser(), name, "Updated to: " + newName + " - " + newDesc);
            } else {
                sys.getAuditLog().log("ROLE_UPDATE_CANCELLED", sys.getCurrentUser(), name, "No changes provided");
            }

            System.out.println("Роль обновлена");
        });

        parser.registerCommand("role-delete", "Удалить роль", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(r==null){
                System.out.println("Не найдена");
                sys.getAuditLog().log("ROLE_DELETE_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(r);
            List<User> users = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                users.add(ra.user());
            }
            if(!users.isEmpty()){
                System.out.println("Роль назначена пользователям:");
                users.forEach(u->System.out.println(" - "+u.username()));
            }
            System.out.print("Подтвердить удаление (да): "); String c = s.nextLine().trim();
            if(!"да".equalsIgnoreCase(c)){
                System.out.println("Отмена");
                sys.getAuditLog().log("ROLE_DELETE_CANCELLED", sys.getCurrentUser(), name, "Deletion cancelled");
                return;
            }
            sys.getRoleManager().remove(r);
            sys.getAuditLog().log("ROLE_DELETE", sys.getCurrentUser(), name, "Deleted role with " + r.getPermissions().size() + " permissions");
            System.out.println("Удалено");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(r==null){
                System.out.println("Не найдена");
                sys.getAuditLog().log("PERMISSION_ADD_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }
            System.out.print("Name: "); String p_name = s.nextLine().trim();
            System.out.print("Resource: "); String res = s.nextLine().trim();
            System.out.print("Description: "); String p_desc = s.nextLine().trim();
            Permission p = new Permission(p_name, res, p_desc);
            r.addPermission(p);
            sys.getAuditLog().log("PERMISSION_ADD", sys.getCurrentUser(), name, "Added permission: " + p_name + " on " + res);
            System.out.println("Право добавлено");
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(r==null){
                System.out.println("Не найдена");
                sys.getAuditLog().log("PERMISSION_REMOVE_FAILED", sys.getCurrentUser(), name, "Role not found");
                return;
            }
            List<Permission> perms = new ArrayList<>(r.getPermissions());
            for(int i=0;i<perms.size();i++) System.out.printf("%d: %s on %s and %s%n", i+1, perms.get(i).name(), perms.get(i).resource(), perms.get(i).description());
            System.out.print("Выберите номер права для удаления: "); int idx = Integer.parseInt(s.nextLine().trim())-1;
            if(idx<0 || idx>=perms.size()){
                System.out.println("Неверно");
                sys.getAuditLog().log("PERMISSION_REMOVE_FAILED", sys.getCurrentUser(), name, "Invalid permission index");
                return;
            }
            Permission removed = perms.get(idx);
            r.removePermission(removed);
            sys.getAuditLog().log("PERMISSION_REMOVE", sys.getCurrentUser(), name, "Removed permission: " + removed.name() + " on " + removed.resource());
            System.out.println("Право удалено");
        });

        parser.registerCommand("role-search", "Поиск ролей", (s, sys) -> {

            System.out.println("1. По имени");
            System.out.println("2. По наличию права");
            System.out.println("3. По минимальному количеству прав");

            System.out.print("Выбор: ");
            String choice = s.nextLine();

            List<Role> roles = sys.getRoleManager().findAll();
            List<Role> result;

            switch (choice) {

                case "1":
                    System.out.print("Введите часть имени: ");
                    String name = s.nextLine().toLowerCase();

                    result = roles.stream()
                            .filter(r -> r.getName().toLowerCase().contains(name))
                            .toList();
                    sys.getAuditLog().log("ROLE_SEARCH", sys.getCurrentUser(), "system", "Searched roles by name: " + name);
                    break;

                case "2":
                    System.out.print("Permission name: ");
                    String pname = s.nextLine();

                    System.out.print("Resource: ");
                    String res = s.nextLine();

                    result = roles.stream()
                            .filter(r -> r.hasPermission(pname, res))
                            .toList();
                    sys.getAuditLog().log("ROLE_SEARCH", sys.getCurrentUser(), "system", "Searched roles by permission: " + pname + " on " + res);
                    break;

                case "3":
                    System.out.print("Минимум прав: ");
                    int min = Integer.parseInt(s.nextLine());

                    result = roles.stream()
                            .filter(r -> r.getPermissions().size() >= min)
                            .toList();
                    sys.getAuditLog().log("ROLE_SEARCH", sys.getCurrentUser(), "system", "Searched roles with min permissions: " + min);
                    break;

                default:
                    System.out.println("Неверный выбор");
                    sys.getAuditLog().log("ROLE_SEARCH_FAILED", sys.getCurrentUser(), "system", "Invalid search option: " + choice);
                    return;
            }

            result.forEach(r ->
                    System.out.println(r.getName() + " | permissions: " + r.getPermissions().size()));
        });

        // =================== Назначения ===================
        parser.registerCommand("assign-role", "Назначить роль пользователю", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("ASSIGN_ROLE_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }
            System.out.println("Доступные роли:");
            sys.getRoleManager().findAll().forEach(r -> System.out.println(" - "+r.getName()));
            System.out.print("Выбор роли: "); String rname = s.nextLine().trim();
            Role role = sys.getRoleManager().findByName(rname).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(role==null){
                System.out.println("Не найдена");
                sys.getAuditLog().log("ASSIGN_ROLE_FAILED", sys.getCurrentUser(), uname, "Role not found: " + rname);
                return;
            }
            System.out.print("Тип (permanent/temporary): "); String t = s.nextLine().trim();
            String expires = null;
            if("temporary".equalsIgnoreCase(t)){
                System.out.print("Дата окончания (yyyy-MM-dd): "); expires = s.nextLine().trim();
            }
            System.out.print("Причина: "); String reason = s.nextLine().trim();
            AssignmentMetadata meta = new AssignmentMetadata(sys.getCurrentUser(), java.time.LocalDate.now().toString(), reason);
            TemporaryAssignment a = new TemporaryAssignment(u, role, meta);
            if(expires!=null) a.expiresAt = expires;
            sys.getAssignmentManager().add(a);
            sys.getAuditLog().log("ASSIGN_ROLE", sys.getCurrentUser(), uname, "Assigned role: " + rname + " (" + t + "), reason: " + reason);
            System.out.println("Роль назначена");
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("REVOKE_ROLE_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);
            for(int i=0;i<assignments.size();i++){
                RoleAssignment ta = assignments.get(i);
                System.out.printf("%d: %s (%s) %s%n", i+1, ta.role().getName(), ta.assignmentType(), ta.isActive()?"active":"expired");
            }
            System.out.print("Выбор назначения для отзыва: "); int idx = Integer.parseInt(s.nextLine().trim())-1;
            if(idx<0 || idx>=assignments.size()){
                System.out.println("Неверно");
                sys.getAuditLog().log("REVOKE_ROLE_FAILED", sys.getCurrentUser(), uname, "Invalid assignment index");
                return;
            }
            TemporaryAssignment ta = (TemporaryAssignment) assignments.get(idx);
            String roleName = ta.role().getName();
            ta.expiresAt = java.time.LocalDate.now().minusDays(1).toString();
            sys.getAuditLog().log("REVOKE_ROLE", sys.getCurrentUser(), uname, "Revoked role: " + roleName);
            System.out.println("Отозвано");
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_LIST", sys.getCurrentUser(), "system", "Viewed all assignments");
            System.out.println("=== Assignments ===");
            sys.getAssignmentManager().findAll().forEach(a ->
                    System.out.printf("%-15s | %-15s | %-10s | %-10s | %s%n",
                            a.user().username(), a.role().getName(), a.assignmentType(), a.isActive()?"active":"expired", a.metadata().assignedAt())
            );
        });

        parser.registerCommand("assignment-list-user", "Назначения пользователя", (s, sys) -> {

            System.out.print("username: ");
            String username = s.nextLine();

            User user = sys.getUserManager()
                    .findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(user);
            sys.getAuditLog().log("ASSIGNMENT_LIST_USER", sys.getCurrentUser(), username, "Viewed assignments for user");

            assignments.forEach(a -> System.out.printf(
                    "%s | %s | %s | %s%n",
                    a.role().getName(),
                    a.assignmentType(),
                    a.isActive() ? "active" : "inactive",
                    a.metadata().assignedAt()
            ));
        });

        parser.registerCommand("assignment-list-role", "Пользователи роли", (s, sys) -> {

            System.out.print("Имя роли: ");
            String name = s.nextLine();

            Role role = sys.getRoleManager()
                    .findByName(name)
                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(role);
            sys.getAuditLog().log("ASSIGNMENT_LIST_ROLE", sys.getCurrentUser(), name, "Viewed users for role");

            assignments.stream()
                    .map(RoleAssignment::user)
                    .forEach(u -> System.out.println(u.username()));
        });

        parser.registerCommand("assignment-active", "Активные назначения", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_ACTIVE", sys.getCurrentUser(), "system", "Viewed active assignments");
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
        });

        parser.registerCommand("assignment-expired", "Истёкшие назначения", (s, sys) -> {
            sys.getAuditLog().log("ASSIGNMENT_EXPIRED", sys.getCurrentUser(), "system", "Viewed expired assignments");
            sys.getAssignmentManager()
                    .findAll()
                    .stream()
                    .filter(a -> !a.isActive())
                    .forEach(a -> System.out.printf(
                            "%s | %s%n",
                            a.user().username(),
                            a.role().getName()
                    ));
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (s, sys) -> {
            System.out.print("Assignment ID: ");
            String aid = s.nextLine().trim();

            RoleAssignment ra = sys.getAssignmentManager()
                    .findById(aid)
                    .orElseThrow(() -> new RuntimeException("Назначение не найдено"));

            if (!(ra instanceof TemporaryAssignment a)) {
                System.out.println("Это не временное назначение, продление невозможно");
                sys.getAuditLog().log("ASSIGNMENT_EXTEND_FAILED", sys.getCurrentUser(), aid, "Not a temporary assignment");
                return;
            }

            System.out.print("Новая дата окончания (yyyy-MM-dd): ");
            String d = s.nextLine().trim();
            a.extend(d);
            sys.getAuditLog().log("ASSIGNMENT_EXTEND", sys.getCurrentUser(), aid, "Extended to: " + d);
            System.out.println("Продлено");
        });

        parser.registerCommand("assignment-search", "Поиск назначений", (s, sys) -> {

            System.out.println("1. По пользователю");
            System.out.println("2. По роли");
            System.out.println("3. По типу");
            System.out.println("4. По статусу");

            System.out.print("Выбор: ");
            String choice = s.nextLine();

            List<RoleAssignment> list = sys.getAssignmentManager().findAll();

            switch (choice) {

                case "1":
                    System.out.print("username: ");
                    String uname = s.nextLine();

                    list.stream()
                            .filter(a -> a.user().username().equals(uname))
                            .forEach(a -> System.out.println(a.role().getName()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system", "Searched assignments by user: " + uname);
                    break;

                case "2":
                    System.out.print("role: ");
                    String role = s.nextLine();

                    list.stream()
                            .filter(a -> a.role().getName().equals(role))
                            .forEach(a -> System.out.println(a.user().username()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system", "Searched assignments by role: " + role);
                    break;

                case "3":
                    System.out.print("type (PERMANENT/TEMPORARY): ");
                    String type = s.nextLine();

                    list.stream()
                            .filter(a -> a.assignmentType().equalsIgnoreCase(type))
                            .forEach(a -> System.out.println(a.user().username()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system", "Searched assignments by type: " + type);
                    break;

                case "4":
                    System.out.print("active? (true/false): ");
                    boolean active = Boolean.parseBoolean(s.nextLine());

                    list.stream()
                            .filter(a -> a.isActive() == active)
                            .forEach(a -> System.out.println(a.user().username()));
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH", sys.getCurrentUser(), "system", "Searched assignments by active status: " + active);
                    break;

                default:
                    sys.getAuditLog().log("ASSIGNMENT_SEARCH_FAILED", sys.getCurrentUser(), "system", "Invalid search option: " + choice);
            }
        });

        // =================== Права ===================
        parser.registerCommand("permissions-user", "Все права пользователя", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("PERMISSIONS_USER_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }
            Map<String,List<String>> permsByResource = new HashMap<>();
            sys.getAssignmentManager().findByUser(u).forEach(a -> a.role().getPermissions().forEach(p -> permsByResource.computeIfAbsent(p.resource(), k->new ArrayList<>()).add(p.name())));
            sys.getAuditLog().log("PERMISSIONS_USER", sys.getCurrentUser(), uname, "Viewed user permissions");
            permsByResource.forEach((res,list) -> System.out.println(res+": "+String.join(", ", list)));
        });

        parser.registerCommand("permissions-check", "Проверить право пользователя", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){
                System.out.println("Не найден");
                sys.getAuditLog().log("PERMISSIONS_CHECK_FAILED", sys.getCurrentUser(), uname, "User not found");
                return;
            }
            System.out.print("Permission name: "); String pname = s.nextLine().trim();
            System.out.print("Resource: "); String res = s.nextLine().trim();
            Optional<RoleAssignment> a = sys.getAssignmentManager().findByUser(u).stream()
                    .filter(as -> as.role().hasPermission(pname, res)).findFirst();
            if(a.isPresent()){
                System.out.println("Есть право через роль: "+a.get().role().getName());
                sys.getAuditLog().log("PERMISSIONS_CHECK", sys.getCurrentUser(), uname, "Checked permission: " + pname + " on " + res + " - GRANTED via " + a.get().role().getName());
            }else{
                System.out.println("Нет права");
                sys.getAuditLog().log("PERMISSIONS_CHECK", sys.getCurrentUser(), uname, "Checked permission: " + pname + " on " + res + " - DENIED");
            }
        });

        // =================== Audit команды ===================
        parser.registerCommand("audit-log", "Просмотр журнала аудита", (s, sys) -> {
            System.out.println("\n=== Audit Log Options ===");
            System.out.println("1. Показать все записи");
            System.out.println("2. Поиск по исполнителю");
            System.out.println("3. Поиск по действию");
            System.out.println("4. Сохранить в файл");
            System.out.print("Выбор: ");

            String choice = s.nextLine().trim();

            switch(choice) {
                case "1":
                    sys.getAuditLog().printLog();
                    sys.getAuditLog().log("AUDIT_VIEW", sys.getCurrentUser(), "system", "Viewed all audit entries");
                    break;

                case "2":
                    System.out.print("Введите имя исполнителя: ");
                    String performer = s.nextLine().trim();
                    List<AuditLog.AuditEntry> byPerformer = sys.getAuditLog().getByPerformer(performer);
                    System.out.println("\n=== Audit entries for performer: " + performer + " ===");
                    byPerformer.forEach(e -> System.out.printf("%s | %s | %s | %s%n",
                            e.timestamp(), e.action(), e.target(), e.details()));
                    System.out.println("Total: " + byPerformer.size());
                    sys.getAuditLog().log("AUDIT_SEARCH", sys.getCurrentUser(), "system", "Searched audit by performer: " + performer);
                    break;

                case "3":
                    System.out.print("Введите действие: ");
                    String action = s.nextLine().trim();
                    List<AuditLog.AuditEntry> byAction = sys.getAuditLog().getByAction(action);
                    System.out.println("\n=== Audit entries for action: " + action + " ===");
                    byAction.forEach(e -> System.out.printf("%s | %s | %s | %s%n",
                            e.timestamp(), e.performer(), e.target(), e.details()));
                    System.out.println("Total: " + byAction.size());
                    sys.getAuditLog().log("AUDIT_SEARCH", sys.getCurrentUser(), "system", "Searched audit by action: " + action);
                    break;

                case "4":
                    System.out.print("Имя файла (например, audit.log): ");
                    String filename = s.nextLine().trim();
                    sys.getAuditLog().saveToFile(filename);
                    sys.getAuditLog().log("AUDIT_SAVE", sys.getCurrentUser(), filename, "Saved audit log to file");
                    break;

                default:
                    System.out.println("Неверный выбор");
                    sys.getAuditLog().log("AUDIT_VIEW_FAILED", sys.getCurrentUser(), "system", "Invalid audit option: " + choice);
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
        });

        parser.registerCommand("clear", "Очистка экрана", (s, sys) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            sys.getAuditLog().log("CLEAR", sys.getCurrentUser(), "system", "Cleared screen");
        });

        parser.registerCommand("exit", "Выход", (s, sys) -> {
            System.out.print("Подтвердить выход (да): "); String c = s.nextLine().trim();
            if("да".equalsIgnoreCase(c)){
                sys.getAuditLog().log("EXIT", sys.getCurrentUser(), "system", "User exited the system");
                System.out.println("Выход");
                System.exit(0);
            } else {
                sys.getAuditLog().log("EXIT_CANCELLED", sys.getCurrentUser(), "system", "Exit cancelled");
            }
        });

        // =================== Reports ===================
        parser.registerCommand("report-users", "Отчёт по всем пользователям с их ролями", (s, sys) -> {
            ReportGenerator reportGen = new ReportGenerator();
            String report = reportGen.generateUserReport(sys.getUserManager(), sys.getAssignmentManager());

            System.out.println("\n" + report);
            sys.getAuditLog().log("REPORT_USERS", sys.getCurrentUser(), "system", "Generated user report");

            System.out.print("Сохранить отчёт в файл? (да/нет): ");
            String answer = s.nextLine().trim();
            if ("да".equalsIgnoreCase(answer)) {
                System.out.print("Имя файла (например, user_report.txt): ");
                String filename = s.nextLine().trim();
                reportGen.exportToFile(report, filename);
                sys.getAuditLog().log("REPORT_USERS_SAVE", sys.getCurrentUser(), filename, "Saved user report to file");
            }
        });

        parser.registerCommand("report-roles", "Отчёт по ролям с количеством пользователей", (s, sys) -> {
            ReportGenerator reportGen = new ReportGenerator();
            String report = reportGen.generateRoleReport(sys.getRoleManager(), sys.getAssignmentManager());

            System.out.println("\n" + report);
            sys.getAuditLog().log("REPORT_ROLES", sys.getCurrentUser(), "system", "Generated role report");

            System.out.print("Сохранить отчёт в файл? (да/нет): ");
            String answer = s.nextLine().trim();
            if ("да".equalsIgnoreCase(answer)) {
                System.out.print("Имя файла (например, role_report.txt): ");
                String filename = s.nextLine().trim();
                reportGen.exportToFile(report, filename);
                sys.getAuditLog().log("REPORT_ROLES_SAVE", sys.getCurrentUser(), filename, "Saved role report to file");
            }
        });

        parser.registerCommand("report-matrix", "Матрица прав (пользователи × ресурсы)", (s, sys) -> {
            ReportGenerator reportGen = new ReportGenerator();
            String report = reportGen.generatePermissionMatrix(sys.getUserManager(), sys.getAssignmentManager());

            System.out.println("\n" + report);
            sys.getAuditLog().log("REPORT_MATRIX", sys.getCurrentUser(), "system", "Generated permission matrix");

            System.out.print("Сохранить отчёт в файл? (да/нет): ");
            String answer = s.nextLine().trim();
            if ("да".equalsIgnoreCase(answer)) {
                System.out.print("Имя файла (например, matrix_report.txt): ");
                String filename = s.nextLine().trim();
                reportGen.exportToFile(report, filename);
                sys.getAuditLog().log("REPORT_MATRIX_SAVE", sys.getCurrentUser(), filename, "Saved permission matrix to file");
            }
        });
    }
}