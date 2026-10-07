package io.github.omegasleepy.openrouter.records;

import java.util.List;
import java.util.Map;

public record JsonSchema(
        String type,
        Map<String, Object> properties,
        List<String> required
) {}