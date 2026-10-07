package io.github.omegasleepy.database.dao;

import io.github.omegasleepy.database.records.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostDao {

    public UUID createPost(Connection conn, UUID authorId, String title, String content) throws SQLException {
        String sql = "INSERT INTO posts (author_id, title, content) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, authorId);
            stmt.setString(2, title);
            stmt.setString(3, content);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }
        }
        throw new SQLException("Failed to create post.");
    }

    public Optional<Post> getPost(Connection conn, UUID id) throws SQLException {
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

    public boolean deletePost(Connection conn, UUID id) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Post> searchPosts(Connection conn, String query, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts WHERE content ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Post> getRecentPosts(Connection conn, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeQueryList(stmt);
        }
    }

    public List<Post> getPostsByCategory(Connection conn, String category, int limit, int offset) throws SQLException {
        String sql = "SELECT p.id, p.author_id, p.title p.content, p.created_at, p.updated_at " +
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

    public List<Post> getPostsByAuthor(Connection conn, UUID authorId, int limit, int offset) throws SQLException {
        String sql = "SELECT id, author_id, title, content, created_at, updated_at " +
                "FROM posts WHERE author_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, authorId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeQueryList(stmt);
        }
    }

    private List<Post> executeQueryList(PreparedStatement stmt) throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                posts.add(mapRowToPost(rs));
            }
        }
        return posts;
    }

    private Post mapRowToPost(ResultSet rs) throws SQLException {
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