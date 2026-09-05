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
                You are an expert banking assistant. 
                Extract the dispute details from the customer's unstructured narrative.
                For the 'disputeCategory' field, you must strictly output one of the following: 
                DUPLICATE_CHARGE, FRAUD, INCORRECT_AMOUNT, or OTHER.
                If the user does not mention a specific data point (like the exact date), leave that field null.
                """;

        return chatClient.prompt()
                .system(systemRules)
                .user(customerNarrative)
                .call()
                .entity(DisputeFormState.class); // <-- This triggers the Structured Output magic
    }
}