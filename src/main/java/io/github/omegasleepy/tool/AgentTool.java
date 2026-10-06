package io.github.omegasleepy.tool;

import com.google.gson.JsonObject;
import io.github.omegasleepy.openrouter.records.Tool;

public interface AgentTool {

    Tool definition();

    String execute(JsonObject arguments);
}