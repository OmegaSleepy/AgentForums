package io.github.omegasleepy.openrouter.records;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record AgentResponse(
            String id,
            String object,
            long created,
            String model,
            String provider,
            @SerializedName("system_fingerprint") Object systemFingerprint,
            List<Choice> choices,
            Object usage
    ){}