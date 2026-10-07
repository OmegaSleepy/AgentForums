package io.github.omegasleepy.llm.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Message(
        String role,
        String content,
        @SerializedName("tool_call_id") String toolCallId,
        @SerializedName("tool_calls") List<ToolCall> toolCalls
) {
    public Message(String role, String content) {
        this(role, content, null, null);
    }

    public Message(String role, String content, String toolCallId) {
        this(role, content, toolCallId, null);
    }
}