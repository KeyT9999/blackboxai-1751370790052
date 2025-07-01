package com.emailreader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    
    // Database configuration - In production, these should be in a properties file
    private static final String URL = "jdbc:postgresql://localhost:5432/emailreader";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL JDBC Driver not found.", e);
            throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("ssl", "false");

            Connection conn = DriverManager.getConnection(URL, props);
            conn.setAutoCommit(true);
            return conn;
        } catch (SQLException e) {
            logger.error("Error connecting to the database", e);
            throw e;
        }
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    logger.error("Error closing resource", e);
                }
            }
        }
    }

    // SQL scripts for creating tables
    public static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            username VARCHAR(50) NOT NULL UNIQUE,
            password_hash VARCHAR(255) NOT NULL,
            role VARCHAR(10) CHECK (role IN ('admin','user'))
        )
    """;

    public static final String CREATE_OUTLOOK_ACCOUNTS_TABLE = """
        CREATE TABLE IF NOT EXISTS outlook_accounts (
            id SERIAL PRIMARY KEY,
            email VARCHAR(100) NOT NULL,
            display_name VARCHAR(100),
            access_token TEXT NOT NULL,
            refresh_token TEXT NOT NULL,
            expires_at TIMESTAMP NOT NULL,
            added_by_admin_id INT REFERENCES users(id)
        )
    """;

    public static void initializeTables() {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute(CREATE_USERS_TABLE);
            conn.createStatement().execute(CREATE_OUTLOOK_ACCOUNTS_TABLE);
            
            // Create default admin user if not exists
            String createAdminSQL = """
                INSERT INTO users (username, password_hash, role)
                SELECT 'admin', '$2a$10$h.dl5J86rGH7I8bD9bZeZe', 'admin'
                WHERE NOT EXISTS (
                    SELECT 1 FROM users WHERE username = 'admin'
                )
            """;
            conn.createStatement().execute(createAdminSQL);
            
            logger.info("Database tables initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database tables", e);
            throw new RuntimeException("Error initializing database tables", e);
        }
    }
}
