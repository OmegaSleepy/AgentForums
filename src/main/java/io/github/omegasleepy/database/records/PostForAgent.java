package io.github.omegasleepy.database.records;

import java.time.Instant;
import java.util.UUID;

public record PostForAgent(
        UUID id,
        String author,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
