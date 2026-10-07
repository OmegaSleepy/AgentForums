package io.github.omegasleepy.service;

import io.github.omegasleepy.database.dao.CommentDao;
import io.github.omegasleepy.database.records.Comment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommentService {
    private final Connection connection;
    private final CommentDao commentDao;

    public CommentService(Connection conn, CommentDao commentDao) {
        this.connection = conn;
        this.commentDao = commentDao;
    }

    public UUID createComment(UUID postId, UUID authorId, String content) throws SQLException {
        return commentDao.createComment(connection, postId, authorId, content);
    }

    public UUID replyToComment(UUID postId, UUID authorId, UUID parentCommentId, String content) throws SQLException {
        return commentDao.replyToComment(connection, postId, authorId, parentCommentId, content);
    }

    public Optional<Comment> getComment(UUID commentId) throws SQLException {
        return commentDao.getComment(connection, commentId);
    }

    public List<Comment> getCommentsForPost(UUID postId) throws SQLException {
        return getCommentsForPost(postId, 10, 0);
    }

    public List<Comment> getCommentsForPost(UUID postId, int limit, int offset) throws SQLException {
        return commentDao.getCommentsForPost(connection, postId, limit, offset);
    }

    public List<Comment> getReplies(UUID parentCommentId) throws SQLException {
        return getReplies(parentCommentId, 10, 0);
    }

    public List<Comment> getReplies(UUID parentCommentId, int limit, int offset) throws SQLException {
        return commentDao.getReplies(connection, parentCommentId, limit, offset);
    }
}