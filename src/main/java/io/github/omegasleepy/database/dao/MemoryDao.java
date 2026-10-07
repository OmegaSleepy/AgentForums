package io.github.omegasleepy.database.dao;

import io.github.omegasleepy.database.records.Memory;
import io.github.omegasleepy.database.records.MemoryType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryDao {

    public UUID createMemory(
            Connection conn,
            UUID agentId,
            String content,
            MemoryType memoryType,
            UUID postId,
            UUID otherAgentId
    ) throws SQLException {
        String sql = """
                INSERT INTO memories (agent_id, content, memory_type, post_id, other_agent_id)
                VALUES (?, ?, ?::memory_type_enum, ?, ?)
                RETURNING id
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, agentId);
            stmt.setString(2, content);
            stmt.setString(3, memoryType.name());
            stmt.setObject(4, postId);
            stmt.setObject(5, otherAgentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }
        }
        throw new SQLException("Failed to create memory.");
    }

    public List<Memory> getMemoriesForAgent(
            Connection conn,
            UUID agentId,
            String query,
            UUID postId,
            UUID otherAgentId,
            int limit
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT id, agent_id, content, memory_type, post_id, other_agent_id, created_at
                FROM memories
                WHERE agent_id = ?
                """);
        if (query != null && !query.isBlank()) {
            sql.append(" AND content ILIKE ?");
        }
        if (postId != null) {
            sql.append(" AND post_id = ?");
        }
        if (otherAgentId != null) {
            sql.append(" AND other_agent_id = ?");
        }
        sql.append(" ORDER BY created_at DESC LIMIT ?");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            stmt.setObject(parameterIndex++, agentId);
            if (query != null && !query.isBlank()) {
                stmt.setString(parameterIndex++, "%" + query + "%");
            }
            if (postId != null) {
                stmt.setObject(parameterIndex++, postId);
            }
            if (otherAgentId != null) {
                stmt.setObject(parameterIndex++, otherAgentId);
            }
            stmt.setInt(parameterIndex, limit);

            List<Memory> memories = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    memories.add(mapRowToMemory(rs));
                }
            }
            return memories;
        }
    }

    private Memory mapRowToMemory(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Memory(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("agent_id"),
                rs.getString("content"),
                MemoryType.valueOf(rs.getString("memory_type")),
                (UUID) rs.getObject("post_id"),
                (UUID) rs.getObject("other_agent_id"),
                createdAt.toInstant()
        );
    }
}
