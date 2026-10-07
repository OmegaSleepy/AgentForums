package io.github.omegasleepy.llm;

import io.github.omegasleepy.data.UserFactory;
import io.github.omegasleepy.database.records.Agent;
import io.github.omegasleepy.service.AgentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AgentScheduler implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(AgentScheduler.class);
    private final AgentService agentService;
    private final UserFactory userFactory;
    private final int maxUsers;
    private final long userGenerationDelay;
    private final TimeUnit userGenerationTimeUnit;
    private final long agentDelay;
    private final TimeUnit agentDelayTimeUnit;
    private final List<Agent> agents = new ArrayList<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private int nextAgent = 0;
    private boolean running = false;

    public AgentScheduler (AgentService agentService, UserFactory userFactory, int maxUsers, long userGenerationDelay, TimeUnit userGenerationTimeUnit, long agentDelay, TimeUnit agentDelayTimeUnit) {
        if (maxUsers <= 0) {
            throw new IllegalArgumentException("maxUsers must be positive");
        }
        this.agentService = agentService;
        this.userFactory = userFactory;
        this.maxUsers = maxUsers;
        this.userGenerationDelay = userGenerationDelay;
        this.userGenerationTimeUnit = userGenerationTimeUnit;
        this.agentDelay = agentDelay;
        this.agentDelayTimeUnit = agentDelayTimeUnit;
        loadAgents();
    }

    private void loadAgents () {
        try {
            agents.addAll(agentService.getActiveAgents(maxUsers, 0));
            logger.info("Loaded {} active agents from database ({}/{})", agents.size(), agents.size(), maxUsers);
        } catch (SQLException e) {
            logger.error("Failed to load agents from database", e);
        }
    }

    public void start () {
        if (running) {
            return;
        }
        running = true;
        executor.execute(this::tick);
    }

    private void tick () {
        if (!running) {
            return;
        }
        try {
            if (agents.size() < maxUsers) {
                generateAgent();
            } else {
                runNextAgent();
            }
        } catch (Exception e) {
            logger.error("Scheduler tick failed", e);
        }
        scheduleNextTick();
    }

    private void generateAgent () throws SQLException {
        userFactory.newAgent(agentService).ifPresent(agent -> {
            agents.add(agent);
            logger.info("Created agent: {} ({}/{})", agent.name(), agents.size(), maxUsers);
        });
    }

    private void runNextAgent () throws IOException, InterruptedException {
        if (agents.isEmpty()) {
            return;
        }
        if (nextAgent >= agents.size()) {
            nextAgent = 0;
        }
        Agent agent = agents.get(nextAgent);
        logger.info("Running agent: {}", agent.name());
        AgentRunner.runAgent(agent);
        nextAgent = (nextAgent + 1) % agents.size();
    }

    private void scheduleNextTick () {
        long delay;
        TimeUnit unit;
        if (agents.size() < maxUsers) {
            delay = userGenerationDelay;
            unit = userGenerationTimeUnit;
        } else {
            delay = agentDelay;
            unit = agentDelayTimeUnit;
        }
        executor.schedule(this::tick, delay, unit);
    }

    public List<Agent> getAgents () {
        return List.copyOf(agents);
    }

    public boolean isRunning () {
        return running;
    }

    public void stop () {
        running = false;
    }

    @Override
    public void close () {
        running = false;
        executor.shutdownNow();
    }
}