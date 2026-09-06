package com.ollamademo.analyzer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogAnalyzerController {

    private static final String SYSTEM_RULES = """
            You are an expert Java and Spring Boot DevOps engineer.
            Analyze the provided stack trace or application log excerpt.
            1. Determine severity: CRITICAL, WARNING, or INFO.
            2. Identify the root cause exception (the innermost "Caused by", not the wrapper) and explain it in plain English, avoiding unnecessary jargon.
            3. Give a concrete, step-by-step recommended fix.
            Base your answer only on what is in the log — do not invent exceptions or line numbers that aren't present.
            """;

    private static final int MAX_LOG_LINES = 200;

    private final ChatClient chatClient;
    private final BeanOutputConverter<LogAnalysisResult> outputConverter =
            new BeanOutputConverter<>(LogAnalysisResult.class);

    public LogAnalyzerController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/analyze")
    public LogAnalysisResult analyzeLog(@RequestBody String stackTrace) {
        return runAnalysis(stackTrace);
    }

    @GetMapping("/analyze-recent")
    public LogAnalysisResult analyzeRecentLogs() throws IOException {
        Path logPath = Path.of("logs/application.log");

        if (!Files.exists(logPath)) {
            return new LogAnalysisResult("INFO", "Log file not found.", "Trigger a crash endpoint first.");
        }

        List<String> allLines = Files.readAllLines(logPath);

        // Find the most recent ERROR line and slice from there, instead of a blind
        // fixed-size tail that can cut a stack trace off mid-way.
        int errorIndex = -1;
        for (int i = allLines.size() - 1; i >= 0; i--) {
            if (allLines.get(i).contains("ERROR")) {
                errorIndex = i;
                break;
            }
        }

        if (errorIndex == -1) {
            return new LogAnalysisResult("INFO", "No ERROR entries found in the log.", "Nothing to diagnose yet.");
        }

        int endIndex = Math.min(allLines.size(), errorIndex + MAX_LOG_LINES);
        String recentLogs = String.join("\n", allLines.subList(errorIndex, endIndex));

        return runAnalysis(recentLogs);
    }

    private LogAnalysisResult runAnalysis(String logContent) {
        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder()
                .model("qwen2.5-coder:3b")
                .temperature(0.0)
                .numCtx(8192)
                .outputSchema(outputConverter.getJsonSchema());

        String response = chatClient.prompt()
                .system(SYSTEM_RULES)
                .user(logContent)
                .options(optionsBuilder)   // ChatClient.options() takes the Builder itself in Spring AI 2.0.x
                .call()
                .content();

        return outputConverter.convert(response);
    }
}