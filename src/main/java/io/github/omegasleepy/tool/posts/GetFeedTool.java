package io.github.omegasleepy.tool.posts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.JsonSchema;
import io.github.omegasleepy.openrouter.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

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
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "page", Map.of(
                                            "type", "integer",
                                            "description", "The feed page to retrieve. First page is with id of `0`"
                                    )
                            ),
                            List.of("page")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        int page = arguments.get("page").getAsInt();

        try {
            String response = gson.toJson(
                    app.postService.getPostsWithAuthors(10, page)
            );

            if (response.length() < 10) {
                return "There are no posts yet. Create one with create_post";
            } else {
                return response;
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}