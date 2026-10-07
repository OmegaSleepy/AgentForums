package io.github.omegasleepy.service;

import io.github.omegasleepy.database.dao.AgentDao;
import io.github.omegasleepy.database.records.Agent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AgentService {
    private final Connection connection;
    private final AgentDao agentDao;

    public AgentService (Connection conn, AgentDao agentDao) {
        this.connection = conn;
        this.agentDao = agentDao;
    }

    public UUID createAgent (String name, String bio, String personality, String model, String systemPrompt, Boolean active) throws SQLException {
        return agentDao.createAgent(connection, name, bio, personality, model, systemPrompt, active);
    }

    public Optional<Agent> getAgent (UUID id) throws SQLException {
        return agentDao.getAgent(connection, id);
    }

    public boolean updateAgent (UUID id, String name, String bio, String personality, String model, String systemPrompt, boolean active) throws SQLException {
        return agentDao.updateAgent(connection, id, name, bio, personality, model, systemPrompt, active);
    }

    public boolean deleteAgent (UUID id) throws SQLException {
        return agentDao.deleteAgent(connection, id);
    }

    public List<Agent> getAllAgents () throws SQLException {
        return getAllAgents(10, 0);
    }

    public List<Agent> getAllAgents (int limit, int offset) throws SQLException {
        return agentDao.getAllAgents(connection, limit, offset);
    }

    public List<Agent> getActiveAgents () throws SQLException {
        return getActiveAgents(10, 0);
    }

    public List<Agent> getActiveAgents (int limit, int offset) throws SQLException {
        return agentDao.getActiveAgents(connection, limit, offset);
    }

    public List<Agent> searchAgentsByName (String query) throws SQLException {
        return searchAgentsByName(query, 10, 0);
    }

    public List<Agent> searchAgentsByName (String query, int limit, int offset) throws SQLException {
        return agentDao.searchAgentsByName(connection, query, limit, offset);
    }

    public List<Agent> getAgentsByModel (String model) throws SQLException {
        return getAgentsByModel(model, 10, 0);
    }

    public List<Agent> getAgentsByModel (String model, int limit, int offset) throws SQLException {
        return agentDao.getAgentsByModel(connection, model, limit, offset);
    }
}