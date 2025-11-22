package fr.iban.survivalcore.utils;

import java.util.Map;

public interface PlaceholderProvider {

    Map<String, String> getPlaceholders();

    default String apply(String input) {
        for (var e : getPlaceholders().entrySet()) {
            input = input.replace("%" + e.getKey() + "%", e.getValue());
        }
        return input;
    }
}
