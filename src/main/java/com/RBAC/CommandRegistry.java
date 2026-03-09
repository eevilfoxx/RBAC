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
            System.out.println("=== Users ===");
            sys.getUserManager().findAll().forEach(u ->
                    System.out.printf("%-15s | %-20s | %-30s%n", u.username(), u.fullName(), u.email()));
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            System.out.print("fullName: "); String fullName = s.nextLine().trim();
            System.out.print("email: "); String email = s.nextLine().trim();
            if(username.isEmpty() || fullName.isEmpty() || email.isEmpty()) { System.out.println("Ошибка: пустые данные"); return; }
            User user = new User(username, fullName, email);
            try { sys.getUserManager().add(user); System.out.println("Пользователь создан"); }
            catch(Exception e){System.out.println("Ошибка: "+e.getMessage());}
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){System.out.println("Не найден"); return;}
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
            if(u==null){System.out.println("Не найден"); return;}
            System.out.print("fullName: "); String fn = s.nextLine().trim();
            System.out.print("email: "); String email = s.nextLine().trim();
            sys.getUserManager().update(username, fn, email);
            System.out.println("Данные обновлены");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (s, sys) -> {
            System.out.print("username: "); String username = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){System.out.println("Не найден"); return;}
            System.out.print("Подтвердите удаление (да): "); String c = s.nextLine().trim();
            if(!"да".equalsIgnoreCase(c)){System.out.println("Отмена"); return;}
            sys.getUserManager().remove(u);
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
                default: System.out.println("Неверный выбор"); return;
            }
            System.out.println("=== Results ===");
            results.forEach(u -> System.out.printf("%-15s | %-20s | %-30s%n", u.username(), u.fullName(), u.email()));
        });

        // =================== Роли ===================
        parser.registerCommand("role-list", "Список всех ролей", (s, sys) -> {
            System.out.println("=== Roles ===");
            sys.getRoleManager().findAll().forEach(r ->
                    System.out.printf("%-15s | Permissions: %d | ID: %s%n", r.getName(), r.getPermissions().size(), r.getId()));
        });

        parser.registerCommand("role-create", "Создать роль", (s, sys) -> {
            System.out.print("Название роли: "); String name = s.nextLine().trim();
            System.out.print("Описание: "); String desc = s.nextLine().trim();
            Role r = new Role(name, desc);
            sys.getRoleManager().add(r);
            System.out.println("Роль создана");
            while(true){
                System.out.print("Добавить право? (да/нет): "); String ans = s.nextLine().trim();
                if(!"да".equalsIgnoreCase(ans)) break;
                System.out.print("Name: "); String p_name = s.nextLine().trim();
                System.out.print("Resource: "); String res = s.nextLine().trim();
                System.out.print("Description: "); String p_desc = s.nextLine().trim();
                Permission p = new Permission(p_name, res, p_desc);
                r.addPermission(p);
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));

            if(r==null){System.out.println("Не найдена"); return;}
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
            }

            System.out.println("Роль обновлена");
        });

        parser.registerCommand("role-delete", "Удалить роль", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(r==null){System.out.println("Не найдена"); return;}
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
            if(!"да".equalsIgnoreCase(c)){System.out.println("Отмена"); return;}
            sys.getRoleManager().remove(r);
            System.out.println("Удалено");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(r==null){System.out.println("Не найдена"); return;}
            System.out.print("Name: "); String p_name = s.nextLine().trim();
            System.out.print("Resource: "); String res = s.nextLine().trim();
            System.out.print("Description: "); String p_desc = s.nextLine().trim();
            Permission p = new Permission(p_name, res, p_desc);
            r.addPermission(p);
            System.out.println("Право добавлено");
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (s, sys) -> {
            System.out.print("Имя роли: "); String name = s.nextLine().trim();
            Role r = sys.getRoleManager().findByName(name).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(r==null){System.out.println("Не найдена"); return;}
            List<Permission> perms = new ArrayList<>(r.getPermissions());
            for(int i=0;i<perms.size();i++) System.out.printf("%d: %s on %s and %sn", i+1, perms.get(i).name(), perms.get(i).resource(), perms.get(i).description());
            System.out.print("Выберите номер права для удаления: "); int idx = Integer.parseInt(s.nextLine().trim())-1;
            if(idx<0 || idx>=perms.size()){System.out.println("Неверно"); return;}
            r.removePermission(perms.get(idx));
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
                    break;

                case "2":
                    System.out.print("Permission name: ");
                    String pname = s.nextLine();

                    System.out.print("Resource: ");
                    String res = s.nextLine();

                    result = roles.stream()
                            .filter(r -> r.hasPermission(pname, res))
                            .toList();
                    break;

                case "3":
                    System.out.print("Минимум прав: ");
                    int min = Integer.parseInt(s.nextLine());

                    result = roles.stream()
                            .filter(r -> r.getPermissions().size() >= min)
                            .toList();
                    break;

                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            result.forEach(r ->
                    System.out.println(r.getName() + " | permissions: " + r.getPermissions().size()));
        });

        // =================== Назначения ===================
        parser.registerCommand("assign-role", "Назначить роль пользователю", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){System.out.println("Не найден"); return;}
            System.out.println("Доступные роли:");
            sys.getRoleManager().findAll().forEach(r -> System.out.println(" - "+r.getName()));
            System.out.print("Выбор роли: "); String rname = s.nextLine().trim();
            Role role = sys.getRoleManager().findByName(rname).orElseThrow(() -> new RuntimeException("Роль не найдена"));
            if(role==null){System.out.println("Не найдена"); return;}
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
            System.out.println("Роль назначена");
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){System.out.println("Не найден"); return;}
            List<RoleAssignment> assignments = sys.getAssignmentManager().findByUser(u);
            for(int i=0;i<assignments.size();i++){
                RoleAssignment ta = assignments.get(i);
                System.out.printf("%d: %s (%s) %s%n", i+1, ta.role().getName(), ta.assignmentType(), ta.isActive()?"active":"expired");
            }
            System.out.print("Выбор назначения для отзыва: "); int idx = Integer.parseInt(s.nextLine().trim())-1;
            if(idx<0 || idx>=assignments.size()){System.out.println("Неверно"); return;}
            TemporaryAssignment ta = (TemporaryAssignment) assignments.get(idx);
            ta.expiresAt = java.time.LocalDate.now().minusDays(1).toString();
            System.out.println("Отозвано");
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (s, sys) -> {
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

            assignments.stream()
                    .map(RoleAssignment::user)
                    .forEach(u -> System.out.println(u.username()));
        });

        parser.registerCommand("assignment-active", "Активные назначения", (s, sys) -> sys.getAssignmentManager()
                .findAll()
                .stream()
                .filter(RoleAssignment::isActive)
                .forEach(a -> System.out.printf(
                        "%s | %s | %s%n",
                        a.user().username(),
                        a.role().getName(),
                        a.assignmentType()
                )));

        parser.registerCommand("assignment-expired", "Истёкшие назначения", (s, sys) -> sys.getAssignmentManager()
                .findAll()
                .stream()
                .filter(a -> !a.isActive())
                .forEach(a -> System.out.printf(
                        "%s | %s%n",
                        a.user().username(),
                        a.role().getName()
                )));

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (s, sys) -> {
            System.out.print("Assignment ID: ");
            String aid = s.nextLine().trim();

            RoleAssignment ra = sys.getAssignmentManager()
                    .findById(aid)
                    .orElseThrow(() -> new RuntimeException("Назначение не найдено"));

            if (!(ra instanceof TemporaryAssignment a)) {
                System.out.println("Это не временное назначение, продление невозможно");
                return;
            }

            System.out.print("Новая дата окончания (yyyy-MM-dd): ");
            String d = s.nextLine().trim();
            a.extend(d);
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
                    break;

                case "2":
                    System.out.print("role: ");
                    String role = s.nextLine();

                    list.stream()
                            .filter(a -> a.role().getName().equals(role))
                            .forEach(a -> System.out.println(a.user().username()));
                    break;

                case "3":
                    System.out.print("type (PERMANENT/TEMPORARY): ");
                    String type = s.nextLine();

                    list.stream()
                            .filter(a -> a.assignmentType().equalsIgnoreCase(type))
                            .forEach(a -> System.out.println(a.user().username()));
                    break;

                case "4":
                    System.out.print("active? (true/false): ");
                    boolean active = Boolean.parseBoolean(s.nextLine());

                    list.stream()
                            .filter(a -> a.isActive() == active)
                            .forEach(a -> System.out.println(a.user().username()));
                    break;
            }
        });

        // =================== Права ===================
        parser.registerCommand("permissions-user", "Все права пользователя", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){System.out.println("Не найден"); return;}
            Map<String,List<String>> permsByResource = new HashMap<>();
            sys.getAssignmentManager().findByUser(u).forEach(a -> a.role().getPermissions().forEach(p -> permsByResource.computeIfAbsent(p.resource(), k->new ArrayList<>()).add(p.name())));
            permsByResource.forEach((res,list) -> System.out.println(res+": "+String.join(", ", list)));
        });

        parser.registerCommand("permissions-check", "Проверить право пользователя", (s, sys) -> {
            System.out.print("username: "); String uname = s.nextLine().trim();
            User u = sys.getUserManager().findByUsername(uname).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            if(u==null){System.out.println("Не найден"); return;}
            System.out.print("Permission name: "); String pname = s.nextLine().trim();
            System.out.print("Resource: "); String res = s.nextLine().trim();
            Optional<RoleAssignment> a = sys.getAssignmentManager().findByUser(u).stream()
                    .filter(as -> as.role().hasPermission(pname, res)).findFirst();
            if(a.isPresent()){
                System.out.println("Есть право через роль: "+a.get().role().getName());
            }else{
                System.out.println("Нет права");
            }
        });

        // =================== Служебные ===================
        parser.registerCommand("help", "Справка по командам", (s, sys) -> parser.printHelp());

        parser.registerCommand("stats", "Статистика системы", (s, sys) -> System.out.println(sys.generateStatistics()));

        parser.registerCommand("clear", "Очистка экрана", (s, sys) -> System.out.println("\033[H\033[2J"));

        parser.registerCommand("exit", "Выход", (s, sys) -> {
            System.out.print("Подтвердить выход (да): "); String c = s.nextLine().trim();
            if("да".equalsIgnoreCase(c)){System.out.println("Выход"); System.exit(0);}
        });
    }
}