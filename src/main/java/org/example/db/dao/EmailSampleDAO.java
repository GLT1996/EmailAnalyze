// EmailSampleDAO.java
package org.example.db.dao;


import org.example.db.DatabaseManager;
import org.example.model.EmailSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmailSampleDAO {
    private static final Logger logger = LoggerFactory.getLogger(EmailSampleDAO.class);
    private final DatabaseManager databaseManager;

    public EmailSampleDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Integer insert(EmailSample sample) {
        String sql = "INSERT INTO email_samples (content, label, confidence_score) VALUES (?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, sample.getContent());
            pstmt.setString(2, sample.getLabel());
            pstmt.setDouble(3, sample.getConfidenceScore());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to insert email sample", e);
        }
        return null;
    }

    public void batchInsert(List<EmailSample> samples) {
        String sql = "INSERT INTO email_samples (content, label) VALUES (?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (EmailSample sample : samples) {
                pstmt.setString(1, sample.getContent());
                pstmt.setString(2, sample.getLabel());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            logger.info("Inserted {} email samples", samples.size());

        } catch (SQLException e) {
            logger.error("Failed to batch insert email samples", e);
        }
    }

    public List<EmailSample> findByLabel(String label, int limit) {
        List<EmailSample> samples = new ArrayList<>();
        String sql = "SELECT id, content, label, confidence_score, created_at, updated_at " +
                "FROM email_samples WHERE label = ? ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, label);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    samples.add(mapResultSetToEmailSample(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to find email samples by label: {}", label, e);
        }
        return samples;
    }

    public List<EmailSample> searchByKeyword(String keyword, String labelFilter) {
        List<EmailSample> samples = new ArrayList<>();
        String sql;

        if (labelFilter != null && !labelFilter.isEmpty()) {
            sql = "SELECT id, content, label, confidence_score, created_at, updated_at " +
                    "FROM email_samples WHERE content LIKE ? AND label = ? ORDER BY created_at DESC";
        } else {
            sql = "SELECT id, content, label, confidence_score, created_at, updated_at " +
                    "FROM email_samples WHERE content LIKE ? ORDER BY created_at DESC";
        }

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");

            if (labelFilter != null && !labelFilter.isEmpty()) {
                pstmt.setString(2, labelFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    samples.add(mapResultSetToEmailSample(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to search email samples with keyword: {}", keyword, e);
        }
        return samples;
    }

    public boolean updateLabel(Integer sampleId, String newLabel) {
        String sql = "UPDATE email_samples SET label = ? WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newLabel);
            pstmt.setInt(2, sampleId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update email sample label", e);
            return false;
        }
    }

    private EmailSample mapResultSetToEmailSample(ResultSet rs) throws SQLException {
        EmailSample sample = new EmailSample();
        sample.setId(rs.getInt("id"));
        sample.setContent(rs.getString("content"));
        sample.setLabel(rs.getString("label"));
        sample.setConfidenceScore(rs.getDouble("confidence_score"));
        sample.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        sample.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return sample;
    }
}