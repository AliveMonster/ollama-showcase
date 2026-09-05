package com.ollamademo.analyzer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogAnalyzerController {

    private final ChatClient chatClient;

    public LogAnalyzerController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/analyze")
    public LogAnalysisResult analyzeLog(@RequestBody String stackTrace) {
        String systemRules = """
                You are an expert Java and Spring Boot DevOps engineer.
                Analyze the provided stack trace or application log.
                1. Determine the severity (CRITICAL, WARNING, or INFO).
                2. Explain the root cause clearly in plain English, avoiding unnecessary jargon.
                3. Provide a step-by-step recommended fix.
                """;

        return chatClient.prompt()
                .system(systemRules)
                .user(stackTrace)
                .call()
                .entity(LogAnalysisResult.class);
    }

    @GetMapping("/analyze-recent")
    public LogAnalysisResult analyzeRecentLogs() throws java.io.IOException {
        java.nio.file.Path logPath = java.nio.file.Path.of("logs/application.log");

        // Safety check if the log file hasn't been generated yet
        if (!java.nio.file.Files.exists(logPath)) {
            return new LogAnalysisResult("INFO", "Log file not found.", "Trigger the crash endpoint first.");
        }

        // Read all lines, but only grab the last 50 to capture the most recent stack trace
        java.util.List<String> allLines = java.nio.file.Files.readAllLines(logPath);
        int startIndex = Math.max(0, allLines.size() - 50);
        String recentLogs = String.join("\n", allLines.subList(startIndex, allLines.size()));

        String systemRules = """
                You are an expert Java and Spring Boot DevOps engineer.
                Analyze the provided log snippet which contains the most recent application events.
                1. Identify the most critical exception or error.
                2. Determine the severity (CRITICAL, WARNING, or INFO).
                3. Explain the root cause clearly in plain English.
                4. Provide a step-by-step recommended fix.
                """;

        return chatClient.prompt()
                .system(systemRules)
                .user(recentLogs)
                .call()
                .entity(LogAnalysisResult.class);
    }
}