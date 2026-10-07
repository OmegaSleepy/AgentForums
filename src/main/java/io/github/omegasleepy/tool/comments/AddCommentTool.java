package io.github.omegasleepy.tool.comments;

import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddCommentTool implements AgentTool {

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "add_comment",
                    "Add a new top-level comment to a post.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "postId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "The UUID of the post you want to comment on."
                                    ),
                                    "content", Map.of(
                                            "type", "string",
                                            "description", "The body text of your comment."
                                    )
                            ),
                            List.of("postId", "content")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        UUID postId = UUID.fromString(
                arguments.get("postId").getAsString()
        );

        UUID authorId = UUID.fromString(
                arguments.get("authorId").getAsString()
        );

        String content = arguments.get("content").getAsString();

        try {
            UUID commentId = Main.app.commentService.createComment(
                    postId,
                    authorId,
                    content
            );

            return "Successfully created a comment with the ID: " + commentId;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}