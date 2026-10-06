package io.github.omegasleepy.tool;

import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.Tool;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreatePostTool implements AgentTool {
    @Override
    public Tool definition () {
        return new Tool(
                "function",
                new FunctionDefinition(
                        "create_post",
                        "Create a new post with a title, contents and topics.",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "title", Map.of(
                                                "type", "string",
                                                "description", "The title of the post."
                                        ),
                                        "contents", Map.of(
                                                "type", "string",
                                                "description", "The contents of the post."
                                        ),
                                        "topics", Map.of(
                                                "type", "array",
                                                "description", "Topics associated with the post.",
                                                "items", Map.of(
                                                        "type", "string"
                                                )
                                        )
                                ),
                                "required", List.of(
                                        "title",
                                        "contents",
                                        "topics"
                                )
                        )
                )
        );
    }

    @Override
    public String execute (JsonObject arguments) {
        UUID authorID = UUID.fromString(arguments.get("authorID").getAsString());
        String title = arguments.get("title").getAsString();
        String content = arguments.get("content").getAsString();

        try {
            return "Successfully created a new post with the ID: " + Main.app.postService.createPost(authorID, title, content);
        } catch (SQLException e) {
            return e.getMessage(); //TODO better error handling
        }
    }
}
