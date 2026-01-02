package com.grpc.order.client;

import com.grpc.payment.grpc.PaymentRequest;
import com.grpc.payment.grpc.PaymentResponse;
import com.grpc.payment.grpc.PaymentServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class PaymentGrpcClient {

    @GrpcClient("payment-service")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

    public String makePayment(String orderId, double amount, String currency) {
        PaymentRequest orderPayment = PaymentRequest.newBuilder()
                .setAmount(amount)
                .setCurrency(currency)
                .setOrderId(orderId)
                .build();
        try {
            PaymentResponse paymentResponse = paymentStub.processPayment(orderPayment);
            return paymentResponse.getSuccess() + " | " + paymentResponse.getTransactionId();
        } catch(StatusRuntimeException ex) {
            return ex.getStatus().getCode() + " | " + ex.getStatus().getDescription();
        }
    }
}
