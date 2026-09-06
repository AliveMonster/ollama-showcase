package com.ollamademo.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;

@RestController
@RequestMapping("/api/payments")
public class PaymentSimulationController {

    private static final Logger log = LoggerFactory.getLogger(PaymentSimulationController.class);

    @GetMapping("/simulate-crash/npe")
    public String simulateNpe() {
        return runAndLog("Payment gateway transaction failed due to nested system exception.",
                this::processPaymentTransaction);
    }

    @GetMapping("/simulate-crash/timeout")
    public String simulateTimeout() {
        return runAndLog("Payment gateway did not respond in time.", () -> {
            throw new RuntimeException("Gateway call failed",
                    new SocketTimeoutException("Read timed out after 5000ms connecting to payments-gateway-service"));
        });
    }

    @GetMapping("/simulate-crash/db-constraint")
    public String simulateDbConstraint() {
        return runAndLog("Failed to persist payment record.", () -> {
            throw new PaymentPersistenceException(
                    "could not execute statement; constraint [uk_payment_reference] violated");
        });
    }

    @GetMapping("/simulate-crash/downstream-4xx")
    public String simulateDownstream4xx() {
        return runAndLog("Downstream fraud-check service rejected the request.", () -> {
            throw new RestClientException(
                    "400 Bad Request from fraud-check-service: missing required field 'customerRiskScore'");
        });
    }

    private String runAndLog(String logMessage, Runnable action) {
        try {
            action.run();
            return "Payment Successful!"; // never reached
        } catch (Exception e) {
            log.error("CRITICAL: {}", logMessage, e);
            return "Payment failed! Check the logs.";
        }
    }

    private void processPaymentTransaction() {
        verifyCustomerAccount();
    }

    private void verifyCustomerAccount() {
        String customerId = null;
        if (customerId.isEmpty()) { // NPE, buried 3 methods deep
            log.info("Account is valid");
        }
    }

    static class PaymentPersistenceException extends RuntimeException {
        PaymentPersistenceException(String message) {
            super(message);
        }
    }
}