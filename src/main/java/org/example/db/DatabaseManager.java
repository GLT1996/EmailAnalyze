// DatabaseManager.java
package org.example.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final String databaseUrl;

    public DatabaseManager(String databasePath) {
        this.databaseUrl = "jdbc:sqlite:" + databasePath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        String[] createTables = {
                // 邮件样本表
                """
            CREATE TABLE IF NOT EXISTS email_samples (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                content TEXT NOT NULL,
                label VARCHAR(50) NOT NULL,
                confidence_score REAL DEFAULT 1.0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """,

                // 标签定义表
                """
            CREATE TABLE IF NOT EXISTS label_definitions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                label_name VARCHAR(50) UNIQUE NOT NULL,
                description TEXT,
                category VARCHAR(50),
                is_active BOOLEAN DEFAULT 1,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """,

                // 邮件分析结果表
                """
            CREATE TABLE IF NOT EXISTS email_analysis_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email_id INTEGER,
                detected_label VARCHAR(50) NOT NULL,
                confidence_score REAL NOT NULL,
                analysis_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (email_id) REFERENCES email_samples(id)
            )
            """
        };

        String createIndexes = """
            CREATE INDEX IF NOT EXISTS idx_email_samples_label ON email_samples(label);
            CREATE INDEX IF NOT EXISTS idx_email_samples_content ON email_samples(content);
            CREATE INDEX IF NOT EXISTS idx_analysis_results_label ON email_analysis_results(detected_label);
            CREATE INDEX IF NOT EXISTS idx_analysis_results_timestamp ON email_analysis_results(analysis_timestamp);
            """;

        String createTrigger = """
            CREATE TRIGGER IF NOT EXISTS update_email_samples_timestamp 
            AFTER UPDATE ON email_samples
            FOR EACH ROW
            BEGIN
                UPDATE email_samples SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
            END;
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 创建表
            for (String sql : createTables) {
                stmt.execute(sql);
            }

            // 创建索引
            stmt.execute(createIndexes);

            // 创建触发器
            stmt.execute(createTrigger);

            // 插入默认标签
            insertDefaultLabels(conn);

            logger.info("Database initialized successfully");

        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void insertDefaultLabels(Connection conn) throws SQLException {
        String[][] defaultLabels = {
                {"promotion", "促销邮件", "commercial"},
                {"spam", "垃圾邮件", "security"},
                {"work", "工作邮件", "business"},
                {"personal", "个人邮件", "personal"},
                {"notification", "通知邮件", "system"},
                {"urgent", "紧急邮件", "priority"},
                {"social", "社交邮件", "personal"},
                {"newsletter", "新闻通讯", "information"}
        };

        String sql = "INSERT OR IGNORE INTO label_definitions (label_name, description, category) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String[] label : defaultLabels) {
                pstmt.setString(1, label[0]);
                pstmt.setString(2, label[1]);
                pstmt.setString(3, label[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }
}