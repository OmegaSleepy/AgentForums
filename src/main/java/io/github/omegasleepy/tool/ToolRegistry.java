package io.github.omegasleepy.tool;

import io.github.omegasleepy.llm.records.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolRegistry {

    private final Map<String, AgentTool> tools = new HashMap<>();

    public void register(AgentTool tool) {
        tools.put(tool.definition().function().name(), tool);
    }

    public AgentTool get(String name) {
        return tools.get(name);
    }

    public List<Tool> definitions() {
        return tools.values().stream()
                .map(AgentTool::definition)
                .toList();
    }
}