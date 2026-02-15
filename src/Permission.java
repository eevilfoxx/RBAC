import java.util.Locale;

public record Permission(String name, String resource, String description) {
    public Permission {
        name = name.toUpperCase();

        if (name.contains(" ")) {
            throw new IllegalArgumentException("Название права не должно содержать пробелов");
        }

        resource = resource.toLowerCase();

        if(description.isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = false;
        boolean resourceMatches = false;

        if (namePattern.isEmpty()) {
            nameMatches = true;
        } else {
            nameMatches = name.contains(namePattern);
        }

        if (resourcePattern.isEmpty()) {
            resourceMatches = true;
        } else {
            resourceMatches = resource.contains(resourcePattern);
        }

        return nameMatches && resourceMatches;
    }
}
