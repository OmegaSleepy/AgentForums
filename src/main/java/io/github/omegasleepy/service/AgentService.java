package io.github.omegasleepy.service;

import io.github.omegasleepy.database.dao.AgentDao;
import io.github.omegasleepy.database.records.Agent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AgentService {
    private final Connection connection;
    private final AgentDao agentDao;

    public AgentService (Connection conn, AgentDao agentDao) {
        this.connection = Objects.requireNonNull(conn, "conn must not be null");
        this.agentDao = Objects.requireNonNull(agentDao, "agentDao must not be null");
    }

    public UUID createAgent (String name, String bio, String personality, String model, String systemPrompt, Boolean active) throws SQLException {
        requireText(name, "name");
        requireText(model, "model");
        return agentDao.createAgent(connection, name, bio, personality, model, systemPrompt, active);
    }

    public Optional<Agent> getAgent (UUID id) throws SQLException {
        Objects.requireNonNull(id, "id must not be null");
        return agentDao.getAgent(connection, id);
    }

    public boolean updateAgent (UUID id, String name, String bio, String personality, String model, String systemPrompt, boolean active) throws SQLException {
        Objects.requireNonNull(id, "id must not be null");
        requireText(name, "name");
        requireText(model, "model");
        return agentDao.updateAgent(connection, id, name, bio, personality, model, systemPrompt, active);
    }

    public boolean deleteAgent (UUID id) throws SQLException {
        Objects.requireNonNull(id, "id must not be null");
        return agentDao.deleteAgent(connection, id);
    }

    public List<Agent> getAllAgents () throws SQLException {
        return getAllAgents(10, 0);
    }

    public List<Agent> getAllAgents (int limit, int offset) throws SQLException {
        validatePagination(limit, offset);
        return agentDao.getAllAgents(connection, limit, offset);
    }

    public List<Agent> getActiveAgents () throws SQLException {
        return getActiveAgents(10, 0);
    }

    public List<Agent> getActiveAgents (int limit, int offset) throws SQLException {
        validatePagination(limit, offset);
        return agentDao.getActiveAgents(connection, limit, offset);
    }

    public List<Agent> searchAgentsByName (String query) throws SQLException {
        return searchAgentsByName(query, 10, 0);
    }

    public List<Agent> searchAgentsByName (String query, int limit, int offset) throws SQLException {
        Objects.requireNonNull(query, "query must not be null");
        validatePagination(limit, offset);
        return agentDao.searchAgentsByName(connection, query, limit, offset);
    }

    public List<Agent> getAgentsByModel (String model) throws SQLException {
        return getAgentsByModel(model, 10, 0);
    }

    public List<Agent> getAgentsByModel (String model, int limit, int offset) throws SQLException {
        Objects.requireNonNull(model, "model must not be null");
        validatePagination(limit, offset);
        return agentDao.getAgentsByModel(connection, model, limit, offset);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    private static void validatePagination(int limit, int offset) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
    }
}