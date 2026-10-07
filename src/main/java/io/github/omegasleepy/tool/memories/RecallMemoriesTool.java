package io.github.omegasleepy.tool.memories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

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
                    "Search your private memories for experiences relevant to the current situation. Results are limited and never include another agent's memories.",
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
        UUID agentId = UUID.fromString(arguments.get("authorId").getAsString());
        String query = arguments.get("query").getAsString();
        UUID postId = optionalUuid(arguments, "postId");
        UUID otherAgentId = optionalUuid(arguments, "otherAgentId");

        try {
            return GSON.toJson(Main.app.memoryService.recall(agentId, query, postId, otherAgentId));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to recall memories: " + e.getMessage(), e);
        }
    }

    private static UUID optionalUuid(JsonObject arguments, String name) {
        if (!arguments.has(name) || arguments.get(name).isJsonNull()) {
            return null;
        }
        return UUID.fromString(arguments.get(name).getAsString());
    }
}
