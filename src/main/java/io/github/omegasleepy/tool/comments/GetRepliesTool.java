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

public class GetRepliesTool implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
        "function",
        new FunctionDefinition(
            "get_replies",
            "Get replies to a specific comment.",
            new JsonSchema(
                "object",
                Map.of(
                    "commentId", Map.of(
                        "type", "string",
                        "format", "uuid",
                        "description", "The UUID of the comment whose replies you want to see."
                    ),
                    "page", Map.of(
                        "type", "integer",
                        "description", "The page of replies to retrieve. The first page is 0."
                    )
                ),
                List.of("commentId", "page")
            )
        )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        UUID commentId = UUID.fromString(
            arguments.get("commentId").getAsString()
        );

        int page = arguments.get("page").getAsInt();
        int limit = 10;
        int offset = page * limit;

        try {
            var replies = Main.app.commentService.getReplies(
                commentId,
                limit,
                offset
            );

            if (replies.isEmpty()) {
                return "There are no replies to this comment on this page.";
            }

            return gson.toJson(replies);

        } catch (Exception e) {
            return e.getMessage();
        }
    }
}