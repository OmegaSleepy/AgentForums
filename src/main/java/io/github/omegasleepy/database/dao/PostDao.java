package io.github.omegasleepy.database.dao;

import io.github.omegasleepy.database.records.Post;
import io.github.omegasleepy.database.records.PostForAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostDao {

    public UUID createPost (Connection conn, UUID authorId, String title, String content, String topic, List<String> categories) throws SQLException {
        String sqlPost = "INSERT INTO posts (author_id, title, content, topic) VALUES (?, ?, ?, ?) RETURNING id";
        UUID postId;

        try (PreparedStatement stmt = conn.prepareStatement(sqlPost)) {
            stmt.setObject(1, authorId);
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.setString(4, topic);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    postId = (UUID) rs.getObject("id");
                } else {
                    throw new SQLException("Failed to create post.");
                }
            }
        }

        if (categories != null && !categories.isEmpty()) {
            String sqlCategory = "INSERT INTO post_categories (post_id, category) VALUES (?, ?::post_category_enum)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCategory)) {
                for (String category : categories) {
                    stmt.setObject(1, postId);
                    stmt.setString(2, category);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        return postId;
    }

    public Optional<Post> getPost (Connection conn, UUID id) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at FROM posts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPost(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean deletePost (Connection conn, UUID id) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Post> searchPosts (Connection conn, String query, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts WHERE content ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Post> getRecentPosts (Connection conn, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeQueryList(stmt);
        }
    }

    public List<PostForAgent> getRecentPostsAndAuthors (Connection conn, int limit, int offset) throws SQLException {
        String sql = """
                SELECT p.id,
                       a."name" AS "author",
                       p.title,
                       p.created_at,
                       p.updated_at
                FROM posts p
                INNER JOIN agents a ON p.author_id = a.id
                ORDER BY p.created_at DESC
                LIMIT ? OFFSET ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeQueryListForAgent(stmt);
        }
    }

    public List<Post> getPostsByCategory (Connection conn, String category, int limit, int offset) throws SQLException {
        String sql = "SELECT p.id, p.author_id, p.title, p.content, p.created_at, p.updated_at " +
                "FROM posts p " +
                "JOIN post_categories pc ON p.id = pc.post_id " +
                "WHERE pc.category = ?::post_category_enum " +
                "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Post> getPostsByTopic (Connection conn, String topic, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts WHERE topic ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, topic);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public List<String> getTopics (Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT topic FROM posts WHERE topic IS NOT NULL ORDER BY topic ASC";
        List<String> topics = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                topics.add(rs.getString("topic"));
            }
        }
        return topics;
    }

    public List<Post> getPostsByAuthor (Connection conn, UUID authorId, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts WHERE author_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, authorId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    private List<Post> executeQueryList (PreparedStatement stmt) throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                posts.add(mapRowToPost(rs));
            }
        }
        return posts;
    }

    private List<PostForAgent> executeQueryListForAgent (PreparedStatement stmt) throws SQLException {
        List<PostForAgent> posts = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                posts.add(mapRowToPostForAgent(rs));
            }
        }
        return posts;
    }

    private PostForAgent mapRowToPostForAgent (ResultSet rs) throws SQLException {
        return new PostForAgent(
                (UUID) rs.getObject("id"),
                rs.getString("author"),
                rs.getString("title"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private Post mapRowToPost (ResultSet rs) throws SQLException {
        return new Post(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("author_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}