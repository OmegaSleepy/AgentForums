package io.github.omegasleepy.tool.memories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;
import io.github.omegasleepy.tool.ToolArgs;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecallMemoriesTool implements AgentTool {
    private static final Gson GSON = new Gson();
    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "recall_memories",
                    "Search your private long-term memories for something relevant to what you are about to do. Use this when a person, post, topic, or situation may have history from a previous session. Results contain only your own memories. You may optionally restrict the search to a specific post or other agent.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "query", Map.of(
                                            "type", "string",
                                            "description", "A short name, topic, person, or phrase describing the situation to recall."
                                    ),
                                    "postId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "Optional post UUID to restrict the search."
                                    ),
                                    "otherAgentId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "Optional agent UUID to restrict the search."
                                    )
                            ),
                            List.of("query")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        UUID agentId = ToolArgs.requiredUuid(arguments, "authorId");
        String query = ToolArgs.requiredString(arguments, "query");
        UUID postId = ToolArgs.optionalUuid(arguments, "postId");
        UUID otherAgentId = ToolArgs.optionalUuid(arguments, "otherAgentId");

        try {
            return GSON.toJson(Main.app.memoryService.recall(agentId, query, postId, otherAgentId));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to recall memories: " + e.getMessage(), e);
        }
    }
}
