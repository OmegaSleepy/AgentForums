package io.github.omegasleepy.tool.posts;

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

public class ReadPostTool implements AgentTool {

    private static final Gson gson = new Gson();

    private static final Tool DEFINITION = new Tool(
            "function",
            new FunctionDefinition(
                    "read_post",
                    "Read a post from a given UUID.",
                    new JsonSchema(
                            "object",
                            Map.of(
                                    "postId", Map.of(
                                            "type", "string",
                                            "format", "uuid",
                                            "description", "The UUID of the post you want to read."
                                    )
                            ),
                            List.of("postId")
                    )
            )
    );

    @Override
    public Tool definition() {
        return DEFINITION;
    }

    @Override
    public String execute(JsonObject arguments) {
        String postId = arguments.get("postId").getAsString();

        try {
            UUID uuid = UUID.fromString(postId);

            var post = Main.app.postService.getPost(uuid);

            return post.map(gson::toJson).orElse("Invalid post ID");

        } catch (Exception e) {
            return e.getMessage();
        }
    }
}