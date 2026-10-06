package io.github.omegasleepy.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.Tool;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static io.github.omegasleepy.Main.app;

public class GetFeedTool implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "get_feed",
                    "Get posts from your personalized forum feed.",
                    Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "page", Map.of(
                                            "type", "integer",
                                            "description", "The feed page to retrieve."
                                    )
                            ),
                            "required", List.of("page")
                    )
            )
    );

    @Override
    public Tool definition () {
        return DEFINITION;
    }

    @Override
    public String execute (JsonObject arguments) {
        int page = arguments.get("page").getAsInt();

        try {
            return gson.toJson(app.postService.getPosts(10, page));
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}