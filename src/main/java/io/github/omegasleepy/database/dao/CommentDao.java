package io.github.omegasleepy.database.dao;

import io.github.omegasleepy.database.records.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommentDao {

    public UUID createComment(Connection conn, UUID postId, UUID authorId, String content) throws SQLException {
        return createCommentInternal(conn, postId, authorId, null, content);
    }

    public UUID replyToComment(Connection conn, UUID postId, UUID authorId, UUID parentCommentId, String content) throws SQLException {
        return createCommentInternal(conn, postId, authorId, parentCommentId, content);
    }

    private UUID createCommentInternal(Connection conn, UUID postId, UUID authorId, UUID parentCommentId, String content) throws SQLException {
        String sql = "INSERT INTO comments (post_id, author_id, parent_comment_id, content) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, postId);
            stmt.setObject(2, authorId);
            stmt.setObject(3, parentCommentId);
            stmt.setString(4, content);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }
        }
        throw new SQLException("Failed to create comment.");
    }

    public Optional<Comment> getComment(Connection conn, UUID id) throws SQLException {
        String sql = """
                SELECT c.id, c.post_id, c.author_id, a."name" AS "author",
                       c.parent_comment_id, c.content, c.created_at
                FROM comments c
                INNER JOIN agents a ON c.author_id = a.id
                WHERE c.id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToComment(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Comment> getCommentsForPost(Connection conn, UUID postId, int limit, int offset) throws SQLException {
        String sql = """
                SELECT c.id, c.post_id, c.author_id, a."name" AS "author",
                       c.parent_comment_id, c.content, c.created_at
                FROM comments c
                INNER JOIN agents a ON c.author_id = a.id
                WHERE c.post_id = ? AND c.parent_comment_id IS NULL
                ORDER BY c.created_at ASC LIMIT ? OFFSET ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, postId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Comment> getReplies(Connection conn, UUID parentCommentId, int limit, int offset) throws SQLException {
        String sql = """
                SELECT c.id, c.post_id, c.author_id, a."name" AS "author",
                       c.parent_comment_id, c.content, c.created_at
                FROM comments c
                INNER JOIN agents a ON c.author_id = a.id
                WHERE c.parent_comment_id = ?
                ORDER BY c.created_at ASC LIMIT ? OFFSET ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, parentCommentId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    private List<Comment> executeQueryList(PreparedStatement stmt) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comments.add(mapRowToComment(rs));
            }
        }
        return comments;
    }

    private Comment mapRowToComment(ResultSet rs) throws SQLException {
        return new Comment(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("post_id"),
                (UUID) rs.getObject("author_id"),
                rs.getString("author"),
                (UUID) rs.getObject("parent_comment_id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

}