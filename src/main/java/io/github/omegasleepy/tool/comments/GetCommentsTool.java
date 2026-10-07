package io.github.omegasleepy.tool.comments;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.llm.records.FunctionDefinition;
import io.github.omegasleepy.llm.records.JsonSchema;
import io.github.omegasleepy.llm.records.Tool;
import io.github.omegasleepy.tool.AgentTool;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetCommentsTool implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
        "function",
        new FunctionDefinition(
            "get_comments",
            "Get the comments on a post.",
            new JsonSchema(
                "object",
                Map.of(
                    "postId", Map.of(
                        "type", "string",
                        "format", "uuid",
                        "description", "The UUID of the post whose comments you want to see."
                    ),
                    "page", Map.of(
                        "type", "integer",
                        "description", "The page of comments to retrieve. The first page is 0."
                    )
                ),
                List.of("postId", "page")
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

        int page = arguments.get("page").getAsInt();
        int limit = 10;
        int offset = page * limit;

        try {
            var comments = Main.app.commentService.getCommentsForPost(
                postId,
                limit,
                offset
            );

            if (comments.isEmpty()) {
                return "There are no comments on this page.";
            }

            return gson.toJson(comments);

        } catch (Exception e) {
            return e.getMessage();
        }
    }
}