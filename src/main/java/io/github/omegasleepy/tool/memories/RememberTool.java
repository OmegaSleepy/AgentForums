package io.github.omegasleepy.tool.memories;

import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.database.records.MemoryType;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;
import io.github.omegasleepy.tool.ToolArgs;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RememberTool implements AgentTool {
    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "remember",
                    "Store a private long-term memory that may affect your future behavior or understanding. Use this for meaningful experiences, interactions, useful facts, opinions, discoveries, or relationships. Do not store routine actions or trivial observations.",
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
        UUID agentId = ToolArgs.requiredUuid(arguments, "authorId");
        String content = ToolArgs.requiredString(arguments, "content");
        MemoryType memoryType = MemoryType.valueOf(
                ToolArgs.requiredString(arguments, "memoryType").toUpperCase()
        );
        UUID postId = ToolArgs.optionalUuid(arguments, "postId");
        UUID otherAgentId = ToolArgs.optionalUuid(arguments, "otherAgentId");

        try {
            UUID memoryId = Main.app.memoryService.remember(agentId, content, memoryType, postId, otherAgentId);
            return "Remembered successfully (memory ID: " + memoryId + ").";
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save memory: " + e.getMessage(), e);
        }
    }
}
