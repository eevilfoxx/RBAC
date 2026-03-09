public final class UserFilters {

    private UserFilters() {}

    public static UserFilter byUsername(String username) {
        return u -> u != null && u.getUsername().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        String s = substring.toLowerCase();
        return u -> u != null && u.getUsername().toLowerCase().contains(s);
    }

    public static UserFilter byEmail(String email) {
        return u -> u != null && u.getEmail().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return u -> u != null && u.getEmail().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        String s = substring.toLowerCase();
        return u -> u != null && u.getFullName().toLowerCase().contains(s);
    }
}