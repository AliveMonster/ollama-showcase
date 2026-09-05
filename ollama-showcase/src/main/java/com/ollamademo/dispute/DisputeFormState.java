
package com.ollamademo.dispute;

public record DisputeFormState(
        String disputeCategory,   // e.g., DUPLICATE_CHARGE, FRAUD, INCORRECT_AMOUNT
        String transactionDate,   // ISO format YYYY-MM-DD
        Double disputeAmount,     // Just the numeric value
        String merchantName,
        Boolean merchantContacted,
        String customerSummary    // AI-generated 1-sentence professional summary of the issue
) {}