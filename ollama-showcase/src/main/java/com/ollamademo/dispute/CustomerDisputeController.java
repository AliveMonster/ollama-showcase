package com.ollamademo.dispute;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disputes")
public class CustomerDisputeController {

    private final ChatClient chatClient;

    // We inject the ChatClient.Builder, which is auto-configured by the Spring AI starter
    public CustomerDisputeController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/autofill")
    public DisputeFormState autoFillForm(@RequestBody String customerNarrative) {

        String systemRules = """
                You are a strict data extraction API. 
                
                BUSINESS RULES:
                1. 'disputeCategory': [FRAUD, DUPLICATE_CHARGE, MERCHANDISE_ISSUE, INCORRECT_AMOUNT].
                2. 'disputeAmount': Number only. No quotes, no currency symbols.
                3. 'merchantContacted': Raw boolean true if the customer reached out to the store (even if the store refused to help). Raw boolean false if they explicitly did not.
                4. 'missingFields': Array of strings. Add "disputeAmount" or "merchantContacted" if they are unknown.
                5. 'recommendedRouting': MORE_QUESTIONS (if missing fields), ESCALATE_TO_AGENT (if FRAUD), DISPUTE_READY (otherwise).
               
                You MUST return ONLY valid JSON matching this exact structure and data types but values based on customer input:
                {
                    "disputeCategory": "MERCHANDISE_ISSUE",
                    "disputeSummary": "Short summary of the issue.",
                    "disputeAmount": 45.99,
                    "merchantContacted": true,
                    "merchantResponse": "They refused to issue a refund.",
                    "missingFields": [],
                    "recommendedRouting": "DISPUTE_READY"
                }
                """;

        return chatClient.prompt()
                .system(systemRules)
                .user(customerNarrative)
                .call()
                .entity(DisputeFormState.class); // <-- This triggers the Structured Output magic
    }
}