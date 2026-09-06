package com.ollamademo.dispute;

import java.util.List;

public record DisputeFormState(
        String disputeCategory,
        String disputeSummary,
        Double disputeAmount,
        Boolean merchantContacted,
        String merchantResponse,
        List<String> missingFields,
        String recommendedRouting
) {}