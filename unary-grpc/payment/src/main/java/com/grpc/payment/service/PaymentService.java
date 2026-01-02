package com.grpc.payment.service;

import com.grpc.payment.exception.PaymentNotFoundException;
import com.grpc.payment.grpc.PaymentRequest;
import com.grpc.payment.grpc.PaymentResponse;
import com.grpc.payment.grpc.PaymentServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@AllArgsConstructor
@GrpcService
public class PaymentService extends PaymentServiceGrpc.PaymentServiceImplBase {

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        try {
            double inrAmount = convertToINR(request.getAmount(), request.getCurrency());
            System.out.println("Payment of Rs. " + inrAmount + " is done successfully");
            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(true)
                    .setTransactionId("txn_" + System.currentTimeMillis())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch(PaymentNotFoundException ex) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(ex.getMessage())
                            .asException()
            );
        }

    }

    private double convertToINR(double amount, String currency) throws PaymentNotFoundException {
        double inrAmount = 0.0;
        switch (currency) {
            case "USD" -> inrAmount = amount * 80;
            case "EURO" -> inrAmount = amount * 100;
            case "JPY" -> inrAmount = amount / 1.57;
            default -> throw new PaymentNotFoundException("No currency found to convert");
        }
        return inrAmount;
    }
}
