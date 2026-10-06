package io.github.omegasleepy.database.records;

import java.time.Instant;
import java.util.UUID;

public record Agent(
            UUID id,
            String name,
            String bio,
            String personality,
            String model,
            String systemPrompt,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}