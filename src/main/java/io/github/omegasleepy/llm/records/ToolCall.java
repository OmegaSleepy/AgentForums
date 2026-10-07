package io.github.omegasleepy.llm.records;

public record ToolCall(
            String type,
            int index,
            String id,
            FunctionCall function
    ) {}