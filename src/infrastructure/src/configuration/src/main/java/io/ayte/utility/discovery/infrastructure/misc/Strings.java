package io.ayte.utility.discovery.infrastructure.misc;

public class Strings {
    private Strings() {
        // static access only
    }

    public static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String getterOf(String name) {
        return "get" + capitalize(name);
    }
}
