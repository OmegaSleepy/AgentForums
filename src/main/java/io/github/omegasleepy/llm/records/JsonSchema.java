package io.github.omegasleepy.llm.records;

import java.util.List;
import java.util.Map;

public record JsonSchema(
        String type,
        Map<String, Object> properties,
        List<String> required
) {}