package org.example.db.dao;

import org.example.db.DatabaseManager;
import org.example.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnalysisResultDAO {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultDAO.class);
    private final DatabaseManager databaseManager;

    public AnalysisResultDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(AnalysisResult result) {
        String sql = "INSERT INTO email_analysis_results (email_id, detected_label, confidence_score) VALUES (?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, result.getEmailId());
            pstmt.setString(2, result.getDetectedLabel());
            pstmt.setDouble(3, result.getConfidenceScore());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to insert analysis result", e);
        }
    }

    public List<AnalysisResult> getLabelStatistics() {
        List<AnalysisResult> stats = new ArrayList<>();
        String sql = "SELECT detected_label, COUNT(*) as count, AVG(confidence_score) as avg_confidence " +
                "FROM email_analysis_results GROUP BY detected_label ORDER BY count DESC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AnalysisResult result = new AnalysisResult();
                result.setDetectedLabel(rs.getString("detected_label"));
                result.setConfidenceScore(rs.getDouble("avg_confidence"));
                // 使用confidenceScore字段存储平均置信度
                stats.add(result);
            }
        } catch (SQLException e) {
            logger.error("Failed to get label statistics", e);
        }
        return stats;
    }
}