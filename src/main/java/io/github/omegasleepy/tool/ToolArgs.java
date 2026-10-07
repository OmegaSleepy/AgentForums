package io.github.omegasleepy.tool;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ToolArgs {

    public static String requiredString(JsonObject args, String name) {
        JsonElement value = requiredValue(args, name);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(
                    "Argument must be a string: " + name
            );
        }

        return value.getAsString();
    }

    public static List<String> requiredStringList(JsonObject args, String name) {
        JsonElement value = requiredValue(args, name);
        if (!value.isJsonArray()) {
            throw new IllegalArgumentException(
                    "Argument must be an array: " + name
            );
        }

        JsonArray array = value.getAsJsonArray();
        List<String> strings = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(
                        "Argument must contain only strings: " + name
                );
            }
            strings.add(element.getAsString());
        }
        return strings;
    }

    public static UUID requiredUuid(JsonObject args, String name) {
        String value = requiredString(args, name);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid UUID for argument: " + name,
                    e
            );
        }
    }

    public static UUID optionalUuid(JsonObject args, String name) {
        if (!args.has(name) || args.get(name).isJsonNull()) {
            return null;
        }
        String value = requiredString(args, name);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid UUID for argument: " + name,
                    e
            );
        }
    }

    public static int requiredInt(JsonObject args, String name) {
        JsonElement value = requiredValue(args, name);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                    "Argument must be an integer: " + name
            );
        }

        try {
            return value.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Argument must be an integer: " + name,
                    e
            );
        }
    }

    public static int requiredNonNegativeInt(JsonObject args, String name) {
        int value = requiredInt(args, name);
        if (value < 0) {
            throw new IllegalArgumentException(
                    "Argument must be non-negative: " + name
            );
        }
        return value;
    }

    private static JsonElement requiredValue(JsonObject args, String name) {
        if (!args.has(name) || args.get(name).isJsonNull()) {
            throw new IllegalArgumentException(
                    "Missing required argument: " + name
            );
        }
        return args.get(name);
    }
}