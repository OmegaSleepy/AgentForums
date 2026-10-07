package io.github.omegasleepy.database.records;

import java.time.Instant;
import java.util.UUID;

public record Memory(
        UUID id,
        UUID agentId,
        String content,
        MemoryType memoryType,
        UUID postId,
        UUID otherAgentId,
        Instant createdAt
) {}
