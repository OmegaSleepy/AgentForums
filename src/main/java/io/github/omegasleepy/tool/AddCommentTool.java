package io.github.omegasleepy.tool;

import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.JsonSchema;
import io.github.omegasleepy.openrouter.records.Tool;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddCommentTool implements AgentTool {

    @Override
    public Tool definition() {
        return new Tool(
                "function",
                new FunctionDefinition(
                        "add_comment",
                        "Add a comment to a post or reply to an existing comment.",
                        new JsonSchema(
                                "object",
                                Map.of(
                                        "postId", Map.of(
                                                "type", "string",
                                                "description", "The UUID of the post being commented on."
                                        ),
                                        "authorId", Map.of(
                                                "type", "string",
                                                "description", "The UUID of the author posting the comment."
                                        ),
                                        "content", Map.of(
                                                "type", "string",
                                                "description", "The body text of the comment."
                                        ),
                                        "parentCommentId", Map.of(
                                                "type", "string",
                                                "description", "Optional. The UUID of the parent comment if this is a reply."
                                        )
                                ),
                                List.of(
                                        "postId",
                                        "authorId",
                                        "content"
                                )
                        )
                )
        );
    }

    @Override
    public String execute(JsonObject arguments) {
        UUID postId = UUID.fromString(arguments.get("postId").getAsString());
        UUID authorId = UUID.fromString(arguments.get("authorId").getAsString());
        String content = arguments.get("content").getAsString();

        UUID parentCommentId = null;
        if (arguments.has("parentCommentId") && !arguments.get("parentCommentId").isJsonNull()) {
            parentCommentId = UUID.fromString(arguments.get("parentCommentId").getAsString());
        }

        try {
            UUID commentId;

            if (parentCommentId != null) {
                commentId = Main.app.commentService.replyToComment(
                        postId,
                        authorId,
                        parentCommentId,
                        content
                );
            } else {
                commentId = Main.app.commentService.createComment(
                        postId,
                        authorId,
                        content
                );
            }

            return "Successfully created a comment with the ID: " + commentId;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}