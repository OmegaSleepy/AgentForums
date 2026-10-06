package io.github.omegasleepy.database;

import io.github.omegasleepy.util.ResourceReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Database {

    private static final List<String> SCHEMA_FILES = List.of(
            "01_schema_agents.sql",
            "02_schema_posts.sql",
            "03_schema_post_categories.sql",
            "04_schema_comments.sql",
            "05_schema_agent_turns.sql"
    );

    /**
     * Initializes the database schema by executing each schema file in order.
     * Uses a single transaction to ensure atomicity.
     *
     * @param connection an active JDBC Connection
     * @throws SQLException if a database access error occurs or execution fails
     * @throws IllegalStateException if a required schema file cannot be read
     */
    public static void initializeSchema(Connection connection) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false); // Begin transaction

            try (Statement statement = connection.createStatement()) {
                for (String schemaFile : SCHEMA_FILES) {
                    String sql = ResourceReader.read(schemaFile)
                            .orElseThrow(() -> new IllegalStateException("Failed to load schema resource: " + schemaFile));

                    statement.execute(sql);
                }
            }

            connection.commit(); // Commit all schemas atomically
        } catch (SQLException | RuntimeException e) {
            connection.rollback(); // Rollback if any schema file fails
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit); // Restore auto-commit state
        }
    }
}