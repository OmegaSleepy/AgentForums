package io.github.omegasleepy.openrouter.records;

import com.google.gson.annotations.SerializedName;

public record Choice(
            int index,
            Object logprobs,
            @SerializedName("finish_reason") String finishReason,
            @SerializedName("native_finish_reason") String nativeFinishReason,
            @SerializedName("message") ConversationMessage message
    ){}