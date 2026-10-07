package io.github.omegasleepy.tool;

import com.google.gson.JsonObject;
import io.github.omegasleepy.openrouter.records.FunctionDefinition;
import io.github.omegasleepy.openrouter.records.Tool;

import java.util.Map;

public class LogOffTool implements AgentTool {
    @Override
    public Tool definition () {
        return new Tool(
                "function",
                new FunctionDefinition(
                        "log_off",
                        "End your session",
                        Map.of()
                )
        );
    }

    @Override
    public String execute (JsonObject arguments) {
        return "nothing";
    }
}
