package com.mahmud.stripepayment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckoutSession {
    private String id;
    private String object;
    private Long amountSubtotal;
    private Long amountTotal;
    private String currency;
    private String customer;
    private String paymentIntent;
    private String payment_status;
    private String status;
    private String mode;
    private String successUrl;
    private String cancelUrl;

    @JsonProperty("metadata")
    private Map<String, String> metadata;
}
