package io.github.omegasleepy.database.dao;

import io.github.omegasleepy.database.records.Agent;

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


}