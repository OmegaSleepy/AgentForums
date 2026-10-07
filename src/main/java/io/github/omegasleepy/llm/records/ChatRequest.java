package io.github.omegasleepy.llm.records;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages,
        Reasoning reasoning,
        List<Tool> tools,
        String toolChoice
) {}