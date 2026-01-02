package com.grpc.order.controller;

import com.grpc.order.client.PaymentGrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final PaymentGrpcClient paymentClient;


    public OrderController(PaymentGrpcClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @GetMapping("/order/pay")
    public String payAndPlaceOrder(
            @RequestParam String orderId,
            @RequestParam double amount,
            @RequestParam String currency) {
        return paymentClient.makePayment(orderId, amount, currency);
    }
}
