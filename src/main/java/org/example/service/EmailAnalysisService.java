// EmailAnalysisService.java
package org.example.service;


import org.example.db.dao.AnalysisResultDAO;
import org.example.db.dao.EmailSampleDAO;
import org.example.model.AnalysisResult;
import org.example.model.EmailAnalysis;
import org.example.model.EmailSample;
import org.example.model.MatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EmailAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(EmailAnalysisService.class);

    private final EmailSampleDAO emailSampleDAO;
    private final AnalysisResultDAO analysisResultDAO;
    private final Map<String, List<String>> analysisRules;

    public EmailAnalysisService(EmailSampleDAO emailSampleDAO, AnalysisResultDAO analysisResultDAO) {
        this.emailSampleDAO = emailSampleDAO;
        this.analysisResultDAO = analysisResultDAO;
        this.analysisRules = initializeDefaultRules();
    }

    private Map<String, List<String>> initializeDefaultRules() {
        Map<String, List<String>> rules = new HashMap<>();

        rules.put("promotion", Arrays.asList("优惠", "折扣", "促销", "特价", "买一送一"));
        rules.put("spam", Arrays.asList("免费", "赢取", "立即点击", "限时优惠", "大奖"));
        rules.put("work", Arrays.asList("会议", "报告", "项目", "deadline", "跟进", "工作计划"));
        rules.put("urgent", Arrays.asList("紧急", "尽快", "重要", "立即处理", "尽快回复"));
        rules.put("notification", Arrays.asList("通知", "提醒", "更新", "系统", "账户"));
        rules.put("personal", Arrays.asList("家人", "朋友", "聚会", "周末", "生日"));

        return rules;
    }

    public EmailAnalysis analyzeEmail(String emailContent) {
        String emailContentLower = emailContent.toLowerCase();
        List<MatchResult> matches = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : analysisRules.entrySet()) {
            String label = entry.getKey();
            List<String> keywords = entry.getValue();

            for (String keyword : keywords) {
                if (emailContentLower.contains(keyword.toLowerCase())) {
                    matches.add(new MatchResult(label, keyword, 0.8));
                    break; // 一个标签匹配一个关键词即可
                }
            }
        }

        EmailAnalysis analysis = new EmailAnalysis(emailContent);
        analysis.setMatches(matches);
        analysis.setMatchCount(matches.size());

        if (!matches.isEmpty()) {
            analysis.setPrimaryLabel(matches.get(0).getLabel());
        }

        return analysis;
    }

    public Integer processAndSaveEmail(String emailContent) {
        // 分析邮件
        EmailAnalysis analysis = analyzeEmail(emailContent);

        // 保存邮件样本
        EmailSample sample = new EmailSample(emailContent, analysis.getPrimaryLabel());
        Integer emailId = emailSampleDAO.insert(sample);

        if (emailId != null) {
            // 保存分析结果
            for (MatchResult match : analysis.getMatches()) {
                AnalysisResult result = new AnalysisResult(emailId, match.getLabel(), match.getConfidence());
                analysisResultDAO.insert(result);
            }
            logger.info("Processed and saved email with ID: {}", emailId);
        }

        return emailId;
    }

    public void batchProcessEmails(List<String> emailContents) {
        for (String content : emailContents) {
            processAndSaveEmail(content);
        }
    }

    public List<EmailSample> getSamplesByLabel(String label, int limit) {
        return emailSampleDAO.findByLabel(label, limit);
    }

    public List<EmailSample> searchSamples(String keyword, String labelFilter) {
        return emailSampleDAO.searchByKeyword(keyword, labelFilter);
    }

    public boolean updateSampleLabel(Integer sampleId, String newLabel) {
        return emailSampleDAO.updateLabel(sampleId, newLabel);
    }

    public void addAnalysisRule(String label, List<String> keywords) {
        analysisRules.put(label, keywords);
        logger.info("Added analysis rule for label: {} with {} keywords", label, keywords.size());
    }

    public Map<String, Object> getAnalysisInsights() {
        Map<String, Object> insights = new HashMap<>();

        // 获取标签统计
        List<AnalysisResult> labelStats = analysisResultDAO.getLabelStatistics();
        insights.put("labelDistribution", labelStats);

        // 可以添加更多统计信息
        insights.put("totalRules", analysisRules.size());
        insights.put("activeLabels", analysisRules.keySet());

        return insights;
    }
}