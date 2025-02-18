package com.mahmud.stripepayment.controller;

import com.mahmud.stripepayment.dto.CheckoutSession;
import com.mahmud.stripepayment.service.StripeJsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/*
    For details
    Go to https://dashboard.stripe.com/test/webhooks/create?endpoint_location=local
    Use Stripe CLI to simulate Stripe events in your local environment or learn more about Webhooks.
    1. Download the CLI and log in with your Stripe account
    run:
        stripe login
    2. Forward events to your webhook
    run:
        stripe listen --forward-to localhost:8080/stripe/webhook
    3. Trigger events with the CLI
    run:
        stripe trigger payment_intent.succeeded
    I am testing locally
*/

@RestController
public class StripeWebhookController {

    private String webhookSecret;

    public StripeWebhookController(@Value("${stripe.webhookSecret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/stripe/webhook")
    public String handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            // Verify the event
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // Handle the event
            switch (event.getType()) {

                case "payment_intent.succeeded": {
                    System.out.println("Payment successful");

                    break;
                }
                
                case "checkout.session.completed":
                    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

                    CheckoutSession parsedSession = StripeJsonParser.parseCheckoutSession(dataObjectDeserializer.getRawJson());

                    System.out.println("User Metadata: " + parsedSession.getMetadata().get("app_username"));
                    System.out.println("Payment Status: " + parsedSession.getPayment_status());
                    // should be paid
                    // if paid, update the order status in your database

                    if (dataObjectDeserializer.getObject().isPresent()) {
                        Session session = (Session) dataObjectDeserializer.getObject().get();
                        handleCheckoutSessionCompleted(session);
                    }
                    break;
                // Handle other event types as needed
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }

            return "Success";
        } catch (SignatureVerificationException e) {
            // Invalid signature
            System.err.println("⚠️  Webhook error while validating signature.");
            return "Invalid signature";
        } catch (Exception e) {
            // System.err.println("Error handling webhook: " + e.getMessage());
            return "Error";
        }
    }

    private void handleCheckoutSessionCompleted(Session session) {
        // not tested
        // Retrieve the session details
        String paymentStatus = session.getPaymentStatus();

        // Example: Update your database or business logic
        if ("paid".equals(paymentStatus)) {
            // TODO: Update order/payment status in your database
        }
    }
}