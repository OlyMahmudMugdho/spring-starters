package com.mahmud.stripepayment.service;

import com.mahmud.stripepayment.dto.ProductRequest;
import com.mahmud.stripepayment.dto.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {


    private String secretKey;

    public StripeService(@Value("${stripe.secretKey}") String secretKey) {
        this.secretKey = secretKey;
    }


    //stripe -API
    //-> productName , amount , quantity , currency
    //-> return sessionId and url



    public StripeResponse checkoutProducts(ProductRequest productRequest) {
        // Set your secret key. Remember to switch to your live secret key in production!
        Stripe.apiKey = secretKey;

        // Create a PaymentIntent with the order amount and currency
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(productRequest.getName())
                        .build();

        // Create new line item with the above product data and associated price
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(productRequest.getCurrency() != null ? productRequest.getCurrency() : "USD")
                        .setUnitAmount(productRequest.getAmount())
                        .setProductData(productData)
                        .build();

        // Create new line item with the above price data
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams
                        .LineItem.builder()
                        .setQuantity(productRequest.getQuantity())
                        .setPriceData(priceData)
                        .build();

        // Custom Field

        // Create new session with the line items
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8080/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .setInvoiceCreation(SessionCreateParams.InvoiceCreation.builder().setEnabled(true).build())
                        .addLineItem(lineItem)
                        .putMetadata("app_username","mugdho_the_user") 
                        .build();
                        // pass metadata to the session, eg: username, email, phone number etc


        // Create new session
        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            System.out.println("session creation failed");
            System.out.println(e.getMessage());
            return null;
        }

        assert session != null;
        return StripeResponse
                .builder()
                .status("SUCCESS")
                .message("Payment session created ")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }

}
