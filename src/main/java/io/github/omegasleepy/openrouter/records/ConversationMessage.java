package io.github.omegasleepy.openrouter.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record ConversationMessage(
            String role,
            String content,
            String refusal,
            String reasoning,
            @SerializedName("tool_calls") List<ToolCall> toolCalls
    ){
    public Message getAsMessage(){
        return new Message(role, content);
    }

}