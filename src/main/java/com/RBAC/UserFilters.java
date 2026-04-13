package com.RBAC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class UserFilters {

    private UserFilters() {}

    public static UserFilter byUsername(String username) {
        return u -> u != null && u.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        String s = substring.toLowerCase();
        return u -> u != null && u.username().toLowerCase().contains(s);
    }

    public static UserFilter byEmail(String email) {
        return u -> u != null && u.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return u -> u != null && u.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        String s = substring.toLowerCase();
        return u -> u != null && u.fullName().toLowerCase().contains(s);
    }

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public List<User> findByFilterParallel(UserFilter filter) {
    return users.values().parallelStream()
            .filter(filter::test)
            .collect(Collectors.toList());
    }

}