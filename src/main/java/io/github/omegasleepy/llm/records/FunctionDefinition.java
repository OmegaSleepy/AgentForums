package io.github.omegasleepy.llm.records;

public record FunctionDefinition(
        String name,
        String description,
        JsonSchema parameters
) {
}