package io.github.omegasleepy.database.dao;

import io.github.omegasleepy.database.records.Agent;
import io.github.omegasleepy.database.records.AgentTurn;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AgentDao {

    public UUID createAgent(Connection conn, String name, String bio, String personality, String model, String systemPrompt, Boolean active) throws SQLException {
        String sql = "INSERT INTO agents (name, bio, personality, model, system_prompt, active) VALUES (?, ?, ?, ?, ?, COALESCE(?, true)) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, bio);
            stmt.setString(3, personality);
            stmt.setString(4, model);
            stmt.setString(5, systemPrompt);
            stmt.setObject(6, active);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }
        }
        throw new SQLException("Failed to create agent.");
    }

    public Optional<Agent> getAgent(Connection conn, UUID id) throws SQLException {
        String sql = "SELECT id, name, bio, personality, model, system_prompt, active, created_at, updated_at FROM agents WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAgent(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateAgent(Connection conn, UUID id, String name, String bio, String personality, String model, String systemPrompt, boolean active) throws SQLException {
        String sql = "UPDATE agents SET name = ?, bio = ?, personality = ?, model = ?, system_prompt = ?, active = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, bio);
            stmt.setString(3, personality);
            stmt.setString(4, model);
            stmt.setString(5, systemPrompt);
            stmt.setBoolean(6, active);
            stmt.setObject(7, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteAgent(Connection conn, UUID id) throws SQLException {
        String sql = "DELETE FROM agents WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Agent> getAllAgents(Connection conn, int limit, int offset) throws SQLException {
        String sql = "SELECT id, name, bio, personality, model, system_prompt, active, created_at, updated_at " +
                "FROM agents ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Agent> getActiveAgents(Connection conn, int limit, int offset) throws SQLException {
        String sql = "SELECT id, name, bio, personality, model, system_prompt, active, created_at, updated_at " +
                "FROM agents WHERE active = true ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Agent> searchAgentsByName(Connection conn, String query, int limit, int offset) throws SQLException {
        String sql = "SELECT id, name, bio, personality, model, system_prompt, active, created_at, updated_at " +
                "FROM agents WHERE name ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Agent> getAgentsByModel(Connection conn, String model, int limit, int offset) throws SQLException {
        String sql = "SELECT id, name, bio, personality, model, system_prompt, active, created_at, updated_at " +
                "FROM agents WHERE model = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, model);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public UUID createAgentTurn(Connection conn, UUID agentId) throws SQLException {
        String sql = "INSERT INTO agent_turns (agent_id) VALUES (?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, agentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }
        }
        throw new SQLException("Failed to create agent turn.");
    }

    public boolean updateAgentTurn(Connection conn, UUID turnId, String status, int actionCount, int toolCallCount) throws SQLException {
        String sql = "UPDATE agent_turns SET finished_at = CURRENT_TIMESTAMP, status = ?::agent_turn_status_enum, " +
                "action_count = ?, tool_call_count = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, actionCount);
            stmt.setInt(3, toolCallCount);
            stmt.setObject(4, turnId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Optional<AgentTurn> getAgentTurn(Connection conn, UUID turnId) throws SQLException {
        String sql = "SELECT id, agent_id, started_at, finished_at, status, action_count, tool_call_count " +
                "FROM agent_turns WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, turnId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAgentTurn(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<AgentTurn> getTurnsByAgentId(Connection conn, UUID agentId, int limit, int offset) throws SQLException {
        String sql = "SELECT id, agent_id, started_at, finished_at, status, action_count, tool_call_count " +
                "FROM agent_turns WHERE agent_id = ? ORDER BY started_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, agentId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            List<AgentTurn> turns = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    turns.add(mapRowToAgentTurn(rs));
                }
            }
            return turns;
        }
    }

    private List<Agent> executeQueryList(PreparedStatement stmt) throws SQLException {
        List<Agent> agents = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                agents.add(mapRowToAgent(rs));
            }
        }
        return agents;
    }

    private Agent mapRowToAgent(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new Agent(
                (UUID) rs.getObject("id"),
                rs.getString("name"),
                rs.getString("bio"),
                rs.getString("personality"),
                rs.getString("model"),
                rs.getString("system_prompt"),
                rs.getBoolean("active"),
                createdAt != null ? createdAt.toInstant() : null,
                updatedAt != null ? updatedAt.toInstant() : null
        );
    }

    private AgentTurn mapRowToAgentTurn(ResultSet rs) throws SQLException {
        Timestamp startedAt = rs.getTimestamp("started_at");
        Timestamp finishedAt = rs.getTimestamp("finished_at");

        return new AgentTurn(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("agent_id"),
                startedAt != null ? startedAt.toInstant() : null,
                finishedAt != null ? finishedAt.toInstant() : null,
                rs.getString("status"),
                rs.getInt("action_count"),
                rs.getInt("tool_call_count")
        );
    }
}