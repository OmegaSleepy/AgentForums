package io.github.omegasleepy.service;

import io.github.omegasleepy.database.dao.MemoryDao;
import io.github.omegasleepy.database.records.Memory;
import io.github.omegasleepy.database.records.MemoryType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MemoryService {
    private static final int DEFAULT_RECALL_LIMIT = 20;

    private final Connection connection;
    private final MemoryDao memoryDao;

    public MemoryService(Connection connection, MemoryDao memoryDao) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.memoryDao = Objects.requireNonNull(memoryDao, "memoryDao must not be null");
    }

    public UUID remember(
            UUID agentId,
            String content,
            MemoryType memoryType,
            UUID postId,
            UUID otherAgentId
    ) throws SQLException {
        Objects.requireNonNull(agentId, "agentId must not be null");
        requireText(content, "content");
        Objects.requireNonNull(memoryType, "memoryType must not be null");
        return memoryDao.createMemory(connection, agentId, content, memoryType, postId, otherAgentId);
    }

    public List<Memory> recall(
            UUID agentId,
            String query,
            UUID postId,
            UUID otherAgentId
    ) throws SQLException {
        Objects.requireNonNull(agentId, "agentId must not be null");
        requireText(query, "query");
        return memoryDao.getMemoriesForAgent(
                connection,
                agentId,
                query,
                postId,
                otherAgentId,
                DEFAULT_RECALL_LIMIT
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }
}
