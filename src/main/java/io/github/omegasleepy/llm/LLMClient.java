package io.github.omegasleepy.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.omegasleepy.database.records.Agent;
import io.github.omegasleepy.llm.records.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static io.github.omegasleepy.Main.app;

public final class LLMClient {

    private static final Logger logger = LoggerFactory.getLogger(LLMClient.class);

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    private LLMClient () {
    }

    public static void run (String apiKey, Agent agent, List<Tool> tools) throws IOException, InterruptedException {

        ChatRequest initialRequest = createRequest(agent, tools);
        List<Message> messages = new ArrayList<>(initialRequest.messages());

        for (int iteration = 0; iteration < 10; iteration++) {

            ChatRequest request = new ChatRequest(
                    initialRequest.model(),
                    List.copyOf(messages),
                    initialRequest.reasoning(),
                    initialRequest.tools(),
                    initialRequest.toolChoice()
            );

            HttpResponse<String> response = fetch(apiKey, request);

            if (response.statusCode() / 100 != 2) {
                throw new IOException(
                        "LLM server returned HTTP " + response.statusCode() + ": " + response.body()
                );
            }

            AgentResponse agentResponse = GSON.fromJson(response.body(), AgentResponse.class);

            if (agentResponse.choices() == null || agentResponse.choices().isEmpty()) {
                throw new IOException("LLM returned no choices.");
            }

            ConversationMessage assistantMessage = agentResponse.choices().getFirst().message();

            if (assistantMessage == null) {
                throw new IOException("LLM returned a null message.");
            }

            List<ToolCall> toolCalls = assistantMessage.toolCalls();

            messages.add(assistantMessage.getAsMessage());

            if (toolCalls == null || toolCalls.isEmpty()) {
                logger.info("[{}] Agent finished: {}", agent.name(), assistantMessage.content());
                return;
            }

            for (ToolCall toolCall : toolCalls) {
                if (toolCall.function() == null) {
                    logger.warn("[{}] Received tool call without function.", agent.name());
                    continue;
                }

                String toolName = toolCall.function().name();

                var tool = app.getToolRegistry().get(toolName);

                if (tool == null) {
                    String result = "Unknown tool: " + toolName;

                    logger.warn("[{}] {}", agent.name(), result);

                    messages.add(new Message("tool", result, toolCall.id()));

                    continue;
                }

                JsonObject arguments;

                try {
                    arguments = GSON.fromJson(toolCall.function().arguments(), JsonObject.class);
                } catch (Exception e) {
                    String result = "Invalid tool arguments: " + e.getMessage();

                    logger.warn("[{}] {}", agent.name(), result, e);

                    messages.add(new Message("tool", result, toolCall.id()));

                    continue;
                }

                arguments.addProperty("authorId", agent.id().toString());

                logger.info(
                        "[{}] Calling {}: {}",
                        agent.name(),
                        toolName,
                        arguments
                );

                String result;

                try {
                    result = tool.execute(arguments);
                } catch (Exception e) {
                    result = "Tool execution failed: " + e.getMessage();

                    logger.error(
                            "[{}] Tool {} failed: {}",
                            agent.name(),
                            toolName,
                            e.getMessage(),
                            e
                    );
                }

                logger.info(
                        "[{}] Result: {}",
                        agent.name(),
                        result
                );

                messages.add(new Message("tool", result, toolCall.id()));

                if (toolName.equals("log_off")) {
                    logger.info("[{}] Logged off.", agent.name());
                    return;
                }
            }
        }

        logger.warn("[{}] Reached maximum iterations.", agent.name());
    }

    private static ChatRequest createRequest (Agent agent, List<Tool> tools) {
        return new ChatRequest(
                agent.model(),
                List.of(
                        new Message(
                                "system",
                                agent.systemPrompt().formatted(
                                        agent.name(),
                                        agent.personality(),
                                        agent.bio()
                                )
                        ),
                        new Message(
                                "user",
                                "You are now active on the forum. Decide what you want to do."
                        )
                ),
                new Reasoning(false),
                tools,
                "required"
        );
    }

    private static HttpResponse<String> fetch (
            String apiKey,
            ChatRequest request
    ) throws IOException, InterruptedException {

        URI uri = URI.create(switch (app.getModelProvider()) {
            case OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions";

            case LOCAL -> app.getLocalLLMURL();
        });

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json");

        if (app.getModelProvider() == ModelProvider.OPENROUTER) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest requestObject = builder
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(request)))
                .build();

        return CLIENT.send(
                requestObject,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}