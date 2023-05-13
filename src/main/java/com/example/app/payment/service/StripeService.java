package com.example.app.payment.service;


import com.example.app.payment.entity.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private Stripe stripe;

    @Value("${STRIPE_SECRET_KEY}")
    private String secretKey;

    @PostConstruct
    public void init() {
        stripe.apiKey = secretKey;
    }

    public ResponseEntity<?> addCard(String cardNumber, String expirationMonth, String expirationYear, String cvc, String email) {
        Stripe.apiKey = secretKey;

        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", cardNumber);
        cardParams.put("exp_month", expirationMonth);
        cardParams.put("exp_year", expirationYear);
        cardParams.put("cvc", cvc);

        Map<String, Object> paymentMethodParams = new HashMap<>();
        paymentMethodParams.put("type", "card");
        paymentMethodParams.put("card", cardParams);

        try {
            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("email", email);
            customerParams.put("payment_method", paymentMethod.getId());

            Customer customer = Customer.create(customerParams);

            return ResponseEntity.ok().body("Card added successfully to customer " + customer.getId());
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> makePaymentUsingCard(String paymentMethodId, String customerId, long amount) {
        Stripe.apiKey = secretKey;

        try {
            Customer customer = Customer.retrieve(customerId);

            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setAmount(amount)
                    .setCurrency("usd")
                    .setPaymentMethod(paymentMethodId)
                    .setCustomer(customerId)
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                    .setConfirm(true)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(createParams);

            return ResponseEntity.ok().body("Payment successful with payment intent ID: " + paymentIntent.getId());
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> charge(PaymentRequest paymentRequest) {
        Stripe.apiKey = secretKey;

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", paymentRequest.getAmount());
        chargeParams.put("currency", paymentRequest.getCurrency());
        chargeParams.put("description", paymentRequest.getDescription());
        chargeParams.put("source", paymentRequest.getSource());

        try {
            Charge charge = Charge.create(chargeParams);

            return ResponseEntity.ok().body("Payment successful with charge ID: " + charge.getId());
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
