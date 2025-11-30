// EmailAnalysisSystem.java
package org.example.service;


import org.example.db.DatabaseManager;
import org.example.db.dao.AnalysisResultDAO;
import org.example.db.dao.EmailSampleDAO;
import org.example.model.EmailSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmailAnalysisSystem {
    private static final Logger logger = LoggerFactory.getLogger(EmailAnalysisSystem.class);

    private final EmailAnalysisService analysisService;

    public EmailAnalysisSystem(String dbPath) {
        DatabaseManager dbManager = new DatabaseManager(dbPath);
        EmailSampleDAO sampleDAO = new EmailSampleDAO(dbManager);
        AnalysisResultDAO resultDAO = new AnalysisResultDAO(dbManager);
        this.analysisService = new EmailAnalysisService(sampleDAO, resultDAO);
    }

    public void demonstrate() {
        logger.info("=== Email Analysis System Demo ===");

        // 添加样本数据
        List<EmailSample> sampleEmails = Arrays.asList(
                new EmailSample("本周五下午3点召开项目进度会议，请准时参加。", "work"),
                new EmailSample("双十一大促销，全场5折起，买一送一！", "promotion"),
                new EmailSample("亲爱的用户，您的账户有重要更新，请及时查看。", "notification"),
                new EmailSample("免费赢取iPhone15，立即点击链接参与！", "spam"),
                new EmailSample("紧急：服务器出现故障，请立即处理！", "urgent"),
                new EmailSample("周末家庭聚会，记得准时到场。", "personal")
        );

        analysisService.batchProcessEmails(
                sampleEmails.stream().map(EmailSample::getContent).collect(Collectors.toList())
        );

        // 分析新邮件
        List<String> testEmails = Arrays.asList(
                "尊敬的客户，我们推出了新的优惠活动，全场商品享受8折优惠！",
                "明天下午2点团队会议，讨论季度报告事宜。",
                "恭喜您获得免费旅游机会，立即回复领取大奖！",
                "系统维护通知：今晚10点至12点进行系统升级。"
        );

        for (String email : testEmails) {
            logger.info("分析邮件: {}", email);
            Integer emailId = analysisService.processAndSaveEmail(email);
            logger.info("保存结果 - 邮件ID: {}", emailId);
        }

        // 查询工作邮件
        logger.info("=== 工作邮件样本 ===");
        List<EmailSample> workEmails = analysisService.getSamplesByLabel("work", 10);
        for (EmailSample sample : workEmails) {
            logger.info("工作邮件: {} (ID: {})", sample.getContent(), sample.getId());
        }

        // 搜索包含"会议"的邮件
        logger.info("=== 搜索'会议'相关的邮件 ===");
        List<EmailSample> meetingEmails = analysisService.searchSamples("会议", null);
        for (EmailSample sample : meetingEmails) {
            logger.info("会议相关: {} -> {}", sample.getContent(), sample.getLabel());
        }

        // 获取分析洞察
        logger.info("=== 分析洞察 ===");
        var insights = analysisService.getAnalysisInsights();
        logger.info("标签分布: {}", insights.get("labelDistribution"));
        logger.info("总规则数: {}", insights.get("totalRules"));
    }

    public static void main(String[] args) {
        EmailAnalysisSystem system = new EmailAnalysisSystem("email_analysis.db");
        system.demonstrate();
    }
}