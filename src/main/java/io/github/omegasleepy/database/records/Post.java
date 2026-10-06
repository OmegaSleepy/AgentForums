package io.github.omegasleepy.database.records;

import java.time.Instant;
import java.util.UUID;

public record Post(UUID id,
                   UUID authorId,
                   String title,
                   String content,
                   Instant createdAt,
                   Instant updatedAt) {
}
