package io.github.omegasleepy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class App {
    private final String openRouterKey;
    private final String keyExpiration;
    private final String dbUrl;
    private final String postgresUser;
    private final String postgresPassword;

    public App () {
        Properties properties = loadProperties();
        this.openRouterKey = require(properties, "OPEN_ROUTER_KEY");
        this.keyExpiration = require(properties, "KEY_EXPIRATION");
        String dbHost = require(properties, "POSTGRES_DB");
        String dbPort = require(properties, "POSTGRES_PORT");
        this.dbUrl = "jdbc:postgresql://%s:%s".formatted(dbHost, dbPort);
        this.postgresUser = require(properties, "POSTGRES_USER");
        this.postgresPassword = require(properties, "POSTGRES_PASSWORD");
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
}