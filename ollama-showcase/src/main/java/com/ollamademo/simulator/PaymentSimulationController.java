package com.ollamademo.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentSimulationController {

    private static final Logger log = LoggerFactory.getLogger(PaymentSimulationController.class);

    @GetMapping("/simulate-crash")
    public String simulateCrash() {
        try {
            processPaymentTransaction();
            return "Payment Successful!"; // This will never happen
        } catch (Exception e) {
            // We catch it and use slf4j to write the massive stack trace into our log file
            log.error("CRITICAL: Payment gateway transaction failed due to nested system exception.", e);
            return "Payment failed! Check the logs.";
        }
    }

    private void processPaymentTransaction() {
        verifyCustomerAccount();
    }

    private void verifyCustomerAccount() {
        String customerId = null;
        // This will throw a NullPointerException buried 3 methods deep
        if (customerId.isEmpty()) {
            System.out.println("Account is valid");
        }
    }
}