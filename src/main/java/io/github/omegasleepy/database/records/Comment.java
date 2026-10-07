package io.github.omegasleepy.database.records;

import java.time.Instant;
import java.util.UUID;

public record Comment(
        UUID id,
        UUID postId,
        UUID authorId,
        String author,
        UUID parentCommentId,
        String content,
        long replyCount,
        Instant createdAt
) {}
