package io.github.omegasleepy.llm;

import io.github.omegasleepy.database.records.Agent;

import java.io.IOException;
import java.sql.SQLException;

import static io.github.omegasleepy.Main.app;

public final class AgentRunner {

    private AgentRunner () {
    }

    public static void runAgent (Agent agent)
            throws IOException, InterruptedException {

        LLMClient.run(
                app.getOpenRouterKey(),
                agent,
                app.getToolRegistry().definitions()
        );
    }

    public static void runAgent (String agentId)
            throws IOException, InterruptedException, SQLException {

        Agent agent = app.agentService
                .getAgent(java.util.UUID.fromString(agentId))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Agent not found: " + agentId
                        )
                );

        runAgent(agent);
    }
}