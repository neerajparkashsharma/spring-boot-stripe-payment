package com.example.app.payment.entity;
import lombok.Data;
@Data
public class PaymentRequest {
    public String source;
    public String amount;
    public String currency;
    public String description;
}


