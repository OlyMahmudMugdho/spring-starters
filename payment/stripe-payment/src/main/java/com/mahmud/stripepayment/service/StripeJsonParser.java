package com.mahmud.stripepayment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahmud.stripepayment.dto.CheckoutSession;

public class StripeJsonParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CheckoutSession parseCheckoutSession(String json) {
        try {
            return objectMapper.readValue(json, CheckoutSession.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

