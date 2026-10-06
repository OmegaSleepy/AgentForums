package io.github.omegasleepy.openrouter.records;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages,
        Reasoning reasoning,
        List<Tool> tools,
        String toolChoice
) {}