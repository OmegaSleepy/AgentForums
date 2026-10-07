package io.github.omegasleepy.tool.posts;

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

public class GetPostsByTopicTool implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "get_posts_by_topic",
                    "Retrieve posts matching a specific topic word (e.g., Linux, Cars).",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "topic", Map.of(
                                            "type", "string",
                                            "description", "The single-word topic to filter by."
                                    ),
                                    "page", Map.of(
                                            "type", "integer",
                                            "description", "Page number starting from 0."
                                    )
                            ),
                            List.of("topic", "page")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        String topic = ToolArgs.requiredString(arguments, "topic");
        int page = ToolArgs.requiredNonNegativeInt(arguments, "page");
        int limit = 10;
        int offset = page * limit;

        try {
            return gson.toJson(Main.app.postService.getPostsByTopic(topic, limit, offset));
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}