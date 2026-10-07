package io.github.omegasleepy.tool.posts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreatePostTool implements AgentTool {

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "create_post",
                    "Create a new post with a title, content, topic, and categories.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "title", Map.of(
                                            "type", "string",
                                            "description", "The title of the post."
                                    ),
                                    "contents", Map.of(
                                            "type", "string",
                                            "description", "The body content of the post."
                                    ),
                                    "topic", Map.of(
                                            "type", "string",
                                            "description", "A single-word topic summarizing the post (e.g. Linux, Cars)."
                                    ),
                                    "categories", Map.of(
                                            "type", "array",
                                            "description", "List of post categories (announcement, general, question, discussion).",
                                            "items", Map.of("type", "string")
                                    )
                            ),
                            List.of("authorId", "title", "contents", "topic", "categories")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        UUID authorID = UUID.fromString(arguments.get("authorId").getAsString());
        String title = arguments.get("title").getAsString();
        String content = arguments.get("contents").getAsString();
        String topic = arguments.get("topic").getAsString();

        List<String> categories = new ArrayList<>();
        if (arguments.has("categories") && !arguments.get("categories").isJsonNull()) {
            JsonArray catArray = arguments.getAsJsonArray("categories");
            catArray.forEach(elem -> categories.add(elem.getAsString()));
        }

        try {
            UUID postId = Main.app.postService.createPost(authorID, title, content, topic, categories);
            return "Successfully created a new post with the ID: " + postId;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}