package io.github.omegasleepy.openrouter.records;

import java.util.Map;

public record FunctionDefinition(
        String name,
        String description,
        Map<String, Object> parameters
) {
}