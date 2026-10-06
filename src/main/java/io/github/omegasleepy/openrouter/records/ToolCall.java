package io.github.omegasleepy.openrouter.records;

public record ToolCall(
            String type,
            int index,
            String id,
            FunctionCall function
    ) {}