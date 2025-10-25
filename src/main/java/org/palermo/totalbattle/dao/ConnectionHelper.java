package org.palermo.totalbattle.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionHelper {
    
    public static Connection getConnection() {
        String url = "jdbc:sqlite:images.db";
        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn == null) {
                throw new RuntimeException("Database created or opened successfully.");
            }
            enableForeignKeys(conn);
            createTextImageTable(conn);
            createLabelTable(conn);
            createLabeledImage(conn);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void createTextImageTable(Connection conn) {
        String sql = """
            CREATE TABLE IF NOT EXISTS TEXT_IMAGE (
                id INTEGER PRIMARY KEY, 
                name TEXT NOT NULL, 
                width INTEGER NOT NULL, 
                height INTEGER NOT NULL, 
                data BLOB
            ); 
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createLabelTable(Connection conn) {
        String sql = """
            CREATE TABLE IF NOT EXISTS LABEL (
                id INTEGER PRIMARY KEY, 
                label TEXT NOT NULL
            ); 
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createLabeledImage(Connection conn) {
        String sql = """
            CREATE TABLE IF NOT EXISTS LABELED_IMAGE (
                id INTEGER PRIMARY KEY, 
                label_id INTEGER REFERENCES LABEL(id), 
                width INTEGER NOT NULL, 
                height INTEGER NOT NULL, 
                data BLOB
            ); 
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enableForeignKeys(Connection conn) {
        String sql = "PRAGMA foreign_keys = ON;";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
