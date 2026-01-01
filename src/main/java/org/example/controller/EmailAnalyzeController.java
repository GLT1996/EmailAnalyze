package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailAnalyzeController {

    @GetMapping("/health")
    public String health() {
        // 简单健康检查接口，后续可以替换为真正的业务逻辑
        return "EmailAnalyze service is running";
    }
}

