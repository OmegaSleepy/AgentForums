package io.github.omegasleepy.tool.comments;

import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;
import io.github.omegasleepy.tool.ToolArgs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddCommentTool implements AgentTool {

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "add_comment",
                    "Add a top-level comment to an existing post. Prefer this over create_post when the existing discussion is relevant to what you want to say.",
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
        UUID postId = ToolArgs.requiredUuid(arguments, "postId");
        UUID authorId = ToolArgs.requiredUuid(arguments, "authorId");
        String content = ToolArgs.requiredString(arguments, "content");

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