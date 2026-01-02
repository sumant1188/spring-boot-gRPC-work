# spring-boot-gRPC-work

This repository contains two standalone Spring Boot modules demonstrating a simple unary gRPC flow:

- `payment` — a gRPC server that implements a `PaymentService` (handles currency conversion and returns a payment result).
- `order` — a Spring Boot app acting as an HTTP front-end that calls the `PaymentService` via a gRPC client.

Both modules are simple, focused examples intended to show how to wire a Spring Boot gRPC server and client using the net.devh Spring Boot gRPC library.

Summary
-------

- Protobuf: Both modules share the same `payment-service.proto` (located under `*/src/main/proto/`). The proto defines:
  - service: `PaymentService`
  - rpc: `ProcessPayment(PaymentRequest) returns (PaymentResponse)`
  - messages: `PaymentRequest` (orderId, amount, currency, paymentDetails), `PaymentResponse` (success, transactionId, errorMessage)

- Unary flow: The `order` module receives an HTTP request and uses a generated gRPC blocking stub to call `PaymentService.ProcessPayment`. `payment` receives the gRPC request, performs a currency conversion, and returns a `PaymentResponse`.

File locations (key files)
-------------------------

- order/
  - src/main/proto/payment-service.proto
  - src/main/resources/application.yml (HTTP server port and gRPC client address)
  - src/main/java/com/grpc/order/OrderServiceApplication.java
  - src/main/java/com/grpc/order/controller/OrderController.java (HTTP endpoint `/order/pay`)
  - src/main/java/com/grpc/order/client/PaymentGrpcClient.java (gRPC client using @GrpcClient)
  - pom.xml, mvnw, mvnw.cmd

- payment/
  - src/main/proto/payment-service.proto
  - src/main/resources/application.yml (gRPC server port)
  - src/main/java/com/grpc/payment/PaymentServiceApplication.java
  - src/main/java/com/grpc/payment/service/PaymentService.java (gRPC server implementation)
  - src/main/java/com/grpc/payment/exception/PaymentNotFoundException.java
  - pom.xml, mvnw, mvnw.cmd

Ports / Addresses
-----------------

- payment module (gRPC server) default port: 9090 (configured in `payment/src/main/resources/application.yml`).
- order module (HTTP server with gRPC client) default HTTP port: 8081 (configured in `order/src/main/resources/application.yml`).
  - The order module's gRPC client for `payment-service` is configured to `static://localhost:9090`.

Build and run (Windows PowerShell)
----------------------------------

You can build and run each module independently. Start the `payment` (gRPC server) first, then the `order` app.

1) Build both modules (optional):

```powershell
# From repo root
.\order\mvnw.cmd -f .\order clean package
.\payment\mvnw.cmd -f .\payment clean package
```

2) Run the `payment` gRPC server:

```powershell
cd .\payment
.\mvnw.cmd spring-boot:run
# Or run the packaged jar
# java -jar target\payment-0.0.1-SNAPSHOT.jar
```

You should see the gRPC server listening on port 9090.

3) Run the `order` HTTP app (which calls the payment gRPC service):

```powershell
cd ..\order
.\mvnw.cmd spring-boot:run
# Or run the packaged jar
# java -jar target\order-0.0.1-SNAPSHOT.jar
```

The `order` app will listen on port 8081 and will connect its gRPC client to `localhost:9090`.

Quick test: HTTP request to order
--------------------------------

After both services are running, you can test the flow by calling the order HTTP endpoint. Example using PowerShell's Invoke-WebRequest (or use a browser):

```powershell
Invoke-WebRequest "http://localhost:8081/order/pay?orderId=order123&amount=10&currency=USD" -UseBasicParsing
```

Expected response (body) looks like:

```
true | txn_1672531200000
```

This response means the order app called the payment gRPC service and received a successful payment response.

Direct gRPC test (grpcurl)
--------------------------

If you want to call the gRPC server directly, you can use grpcurl (or any gRPC client). Example with grpcurl (replace the proto import path if needed):

```powershell
# Example grpcurl call (requires grpcurl to be installed)
grpcurl -plaintext -d '{"orderId":"o1","amount":5,"currency":"USD"}' localhost:9090 payment.PaymentService/ProcessPayment
```

Troubleshooting
---------------

- Order app fails to connect to payment service: ensure `payment` is running on port 9090 and that `order/src/main/resources/application.yml` points to `static://localhost:9090`.
- Currency not supported: `PaymentService` throws NOT_FOUND for unsupported currency codes (supported: USD, EURO, JPY). The order client will return the gRPC status code and description in case of errors.
- Protobuf code generation: The project uses the protobuf maven plugin during build. If you see missing generated classes, run `mvnw.cmd -f <module> clean package` to trigger generation.

Tests
-----

Each module contains unit tests under `src/test/java`. You can run them with:

```powershell
.\order\mvnw.cmd -f .\order test
.\payment\mvnw.cmd -f .\payment test
```

Notes and next steps
--------------------

- This is a unary gRPC example (single request — single response). If you want to extend it to streaming or bidirectional flows, I can help update the proto and implementations.
- If you'd like a single script to build and start both services in order, I can add PowerShell scripts or a Windows batch file.

If you want any changes (more examples, README formatting, or a run script), tell me which and I will add them.
