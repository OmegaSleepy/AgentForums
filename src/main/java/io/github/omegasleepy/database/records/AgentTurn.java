package io.github.omegasleepy.database.records;

import java.time.Instant;
import java.util.UUID;

public record AgentTurn(
        UUID id,
        UUID agentId,
        Instant startedAt,
        Instant finishedAt,
        String status,
        int actionCount,
        int toolCallCount
) {}