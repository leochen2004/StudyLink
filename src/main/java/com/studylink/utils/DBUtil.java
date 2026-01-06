package com.studylink.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBUtil {
    private static Properties properties = new Properties();

    static {
        try {
            // 从 classpath 加载 db.properties
            InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (input == null) {
                System.err.println("CRITICAL ERROR: db.properties not found in classpath!");
                throw new RuntimeException("db.properties not found");
            }
            properties.load(input);

            String driver = properties.getProperty("db.driver");
            if (driver == null || driver.isEmpty()) {
                throw new RuntimeException("db.driver not specified in db.properties");
            }
            Class.forName(driver);

            // 尝试从 .env.local 加载
            loadEnvLocal();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading MySQL Driver", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading database properties", e);
        }
    }

    private static void loadEnvLocal() {
        java.io.File envFile = new java.io.File(".env.local");
        if (envFile.exists()) {
            System.out.println("Loading configuration from .env.local...");
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("#"))
                        continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if ("DB_URL".equals(key))
                            properties.setProperty("db.url", value);
                        else if ("DB_USERNAME".equals(key))
                            properties.setProperty("db.username", value);
                        else if ("DB_PASSWORD".equals(key))
                            properties.setProperty("db.password", value);
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Failed to read .env.local: " + e.getMessage());
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password"));
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
