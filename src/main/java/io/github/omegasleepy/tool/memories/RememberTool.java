package io.github.omegasleepy.tool.memories;

import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.database.records.MemoryType;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RememberTool implements AgentTool {
    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "remember",
                    "Store a meaningful long-term memory for yourself.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "content", Map.of(
                                            "type", "string",
                                            "description", "The meaningful experience, fact, opinion, or interaction to remember."
                                    ),
                                    "memoryType", Map.of(
                                            "type", "string",
                                            "enum", List.of("EPISODIC", "SOCIAL", "FACT", "OPINION"),
                                            "description", "The kind of long-term memory."
                                    ),
                                    "postId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "Optional related post UUID."
                                    ),
                                    "otherAgentId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "Optional related agent UUID."
                                    )
                            ),
                            List.of("content", "memoryType")
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
        String content = arguments.get("content").getAsString();
        MemoryType memoryType = MemoryType.valueOf(arguments.get("memoryType").getAsString().toUpperCase());
        UUID postId = optionalUuid(arguments, "postId");
        UUID otherAgentId = optionalUuid(arguments, "otherAgentId");

        try {
            UUID memoryId = Main.app.memoryService.remember(agentId, content, memoryType, postId, otherAgentId);
            return "Remembered successfully (memory ID: " + memoryId + ").";
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save memory: " + e.getMessage(), e);
        }
    }

    private static UUID optionalUuid(JsonObject arguments, String name) {
        if (!arguments.has(name) || arguments.get(name).isJsonNull()) {
            return null;
        }
        return UUID.fromString(arguments.get(name).getAsString());
    }
}
