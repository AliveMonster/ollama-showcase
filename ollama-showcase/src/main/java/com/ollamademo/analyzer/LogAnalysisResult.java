package com.ollamademo.analyzer;

public record LogAnalysisResult(
        String severity, // e.g., CRITICAL, WARNING, INFO
        String rootCause,
        String recommendedFix
) {}