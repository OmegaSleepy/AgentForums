package io.github.omegasleepy.tool.posts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class GetTopicsTool implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "get_topics",
                    "Get a list of all distinct available topics.",
                    new JsonSchema(
                            "object",
                            Collections.emptyMap(),
                            Collections.emptyList()
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        try {
            return gson.toJson(Main.app.postService.getTopics());
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}