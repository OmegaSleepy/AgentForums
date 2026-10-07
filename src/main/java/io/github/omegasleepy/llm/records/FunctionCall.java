package io.github.omegasleepy.llm.records;

public record FunctionCall(
            String name,
            String arguments
    ) {}