package io.github.omegasleepy;

import io.github.omegasleepy.database.Database;
import io.github.omegasleepy.database.dao.AgentDao;
import io.github.omegasleepy.database.dao.CommentDao;
import io.github.omegasleepy.database.dao.PostDao;
import io.github.omegasleepy.llm.ModelProvider;
import io.github.omegasleepy.service.AgentService;
import io.github.omegasleepy.service.CommentService;
import io.github.omegasleepy.service.PostService;
import io.github.omegasleepy.tool.*;
import io.github.omegasleepy.tool.comments.*;
import io.github.omegasleepy.tool.misc.LogOffTool;
import io.github.omegasleepy.tool.posts.CreatePostTool;
import io.github.omegasleepy.tool.posts.GetFeedTool;
import io.github.omegasleepy.tool.posts.ReadPostTool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class App {
    private final String openRouterKey;
    private final String keyExpiration;
    private final String dbUrl;
    private final String postgresUser;
    private final String postgresPassword;

    private final ModelProvider modelProvider;
    private final String localLLMURL;

    public final PostService postService;
    public final CommentService commentService;
    public final AgentService agentService;

    private final Connection connection;
    private final ToolRegistry toolRegistry;

    public App () throws SQLException {
        Properties properties = loadProperties();
        this.openRouterKey = require(properties, "OPEN_ROUTER_KEY");
        this.keyExpiration = require(properties, "KEY_EXPIRATION");

        String dbHost = require(properties, "POSTGRES_HOST");
        String dbPort = require(properties, "POSTGRES_PORT");
        String dbName = require(properties, "POSTGRES_DB");

        modelProvider = ModelProvider.valueOf(require(properties, "MODEL_PROVIDER").toUpperCase());
        this.localLLMURL = require(properties, "LOCAL_LLM_URL");


        this.dbUrl = "jdbc:postgresql://%s:%s/%s".formatted(dbHost, dbPort, dbName);

        this.postgresUser = require(properties, "POSTGRES_USER");
        this.postgresPassword = require(properties, "POSTGRES_PASSWORD");

        this.connection = DriverManager.getConnection(this.dbUrl, this.postgresUser, this.postgresPassword);

        this.postService = new PostService(connection, new PostDao());
        this.commentService = new CommentService(connection, new CommentDao());
        this.agentService = new AgentService(connection, new AgentDao());

        this.toolRegistry = new ToolRegistry();
        populateToolRegistry();

        Database.initializeSchema(connection);
    }

    private void populateToolRegistry () {
        toolRegistry.register(new CreatePostTool());
        toolRegistry.register(new GetFeedTool());
        toolRegistry.register(new LogOffTool());
        toolRegistry.register(new AddCommentTool());
        toolRegistry.register(new GetCommentsTool());
        toolRegistry.register(new GetRepliesTool());
        toolRegistry.register(new ReadComments());
        toolRegistry.register(new ReplyToCommentTool());
        toolRegistry.register(new ReadPostTool());
    }

    private static Properties loadProperties () {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of(".env"))) {
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load .env", e);
        }
    }

    private static String require (Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration property: " + key);
        }
        return value;
    }

    public String getLocalLLMURL () {
        return localLLMURL;
    }

    public ModelProvider getModelProvider () {
        return modelProvider;
    }

    public String getOpenRouterKey () {
        return openRouterKey;
    }

    public String getKeyExpiration () {
        return keyExpiration;
    }

    public String getDbUrl () {
        return dbUrl;
    }

    public String getPostgresUser () {
        return postgresUser;
    }

    public String getPostgresPassword () {
        return postgresPassword;
    }

    public ToolRegistry getToolRegistry () {
        return toolRegistry;
    }
}