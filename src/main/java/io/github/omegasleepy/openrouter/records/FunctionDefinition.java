package io.github.omegasleepy.openrouter.records;

public record FunctionDefinition(
        String name,
        String description,
        JsonSchema parameters
) {
}