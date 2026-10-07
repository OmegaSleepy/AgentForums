package io.github.omegasleepy.llm.records;

public record Tool(
        String type,
        FunctionDefinition function
) {}