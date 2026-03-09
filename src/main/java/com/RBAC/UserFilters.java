package com.RBAC;

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
}