package io.github.omegasleepy.tool;

import com.google.gson.JsonObject;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.JsonSchema;
import io.github.omegasleepy.openrouter.records.Tool;

import java.util.Map;
import java.util.List;

public class LogOffTool implements AgentTool {

    @Override
    public Tool definition() {
        return new Tool(
                "function",
                new FunctionDefinition(
                        "log_off",
                        "End your session.",
                        new JsonSchema(
                                "object",
                                Map.of(),
                                List.of()
                        )
                )
        );
    }

    @Override
    public String execute(JsonObject arguments) {
        return "nothing";
    }
}