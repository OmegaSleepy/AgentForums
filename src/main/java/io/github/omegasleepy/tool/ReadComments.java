package io.github.omegasleepy.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.Main;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.JsonSchema;
import io.github.omegasleepy.openrouter.records.Tool;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReadComments implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "read_comments",
                    "Read a comment by its UUID.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "commentId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "The UUID of the comment you want to read."
                                    )
                            ),
                            List.of("commentId")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        UUID commentId = UUID.fromString(arguments.get("commentId").getAsString());

        try {
            var comment = Main.app.commentService.getComment(commentId);
            return comment.map(gson::toJson).orElse("Invalid comment ID");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
