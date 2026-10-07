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

public class ReplyToCommentTool implements AgentTool {

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "reply_to_comment",
                    "Reply to an existing comment on a post.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "postId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "The UUID of the post containing the comment."
                                    ),
                                    "commentId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "The UUID of the comment you want to reply to."
                                    ),
                                    "content", Map.of(
                                            "type", "string",
                                            "description", "The body text of your reply."
                                    )
                            ),
                            List.of("postId", "commentId", "content")
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

        UUID commentId = UUID.fromString(
                arguments.get("commentId").getAsString()
        );

        UUID authorId = UUID.fromString(
                arguments.get("authorId").getAsString()
        );

        String content = arguments.get("content").getAsString();

        try {
            UUID replyId = Main.app.commentService.replyToComment(
                    postId,
                    authorId,
                    commentId,
                    content
            );

            return "Successfully created a reply with the ID: " + replyId;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}