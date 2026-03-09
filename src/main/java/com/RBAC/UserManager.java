package com.RBAC;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User user) {
        validate(user);
        if (users.containsKey(user.username())) {
            throw new IllegalArgumentException("User already exists: " + user.username());
        }
        users.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        return users.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User user = users.get(username);
        if (user == null) {
            throw new NoSuchElementException("User not found: " + username);
        }
        if (newFullName == null || newEmail == null) {
            throw new IllegalArgumentException("Invalid user data");
        }

        User new_user = new User(username, newFullName, newEmail);
        add(new_user);

        remove(user);
    }

    private void validate(User user) {
        if (user.username().isBlank())
            throw new IllegalArgumentException("Username is empty");
        if (!user.email().contains("@"))
            throw new IllegalArgumentException("Invalid email");
    }
}