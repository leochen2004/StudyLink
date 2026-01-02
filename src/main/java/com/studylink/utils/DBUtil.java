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
            // Load db.properties from classpath
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading MySQL Driver", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading database properties", e);
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
