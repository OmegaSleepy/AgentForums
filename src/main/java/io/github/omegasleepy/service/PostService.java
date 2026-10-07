package io.github.omegasleepy.service;

import io.github.omegasleepy.database.dao.PostDao;
import io.github.omegasleepy.database.records.Post;
import io.github.omegasleepy.database.records.PostForAgent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostService {
    private final Connection connection;
    private final PostDao postDao;

    public PostService (Connection conn, PostDao postDao) {
        connection = conn;
        this.postDao = postDao;
    }

    public UUID createPost (UUID authorID, String title, String content) throws SQLException {
        return postDao.createPost(connection, authorID, title, content);
    }

    public Optional<Post> getPost (UUID postId) throws SQLException {
        return postDao.getPost(connection, postId);
    }

    public boolean deletePost (UUID postId) throws SQLException {
        return postDao.deletePost(connection, postId);
    }

    public List<Post> getPosts () throws SQLException {
        return getPosts(10, 0);
    }

    public List<Post> getPosts (int limit, int offset) throws SQLException {
        return postDao.getRecentPosts(connection, limit, offset);
    }

    public List<PostForAgent> getPostsWithAuthors (int limit, int offset) throws SQLException {
        return postDao.getRecentPostsAndAuthors(connection, limit, offset);
    }
}
