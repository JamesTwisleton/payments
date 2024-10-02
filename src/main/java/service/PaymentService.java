package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import domain.dto.PaymentRequestDTO;
import domain.entity.Payment;
import domain.entity.PaymentStatus;
import domain.repository.PaymentRepository;
import infrastructure.messaging.KafkaProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final KafkaProducer kafkaProducer;
  private final ExecutorService executorService;
  private final Gson gson = new Gson();

  public PaymentService(PaymentRepository paymentRepository, KafkaProducer kafkaProducer) {
    this.paymentRepository = paymentRepository;
    this.kafkaProducer = kafkaProducer;
    this.executorService = Executors.newFixedThreadPool(10); // Thread pool with 10 threads
  }

  // Handle GET /payments - Return all payments as a JSON response
  public void handleGetPayments(HttpExchange exchange) {

    // Fetch all payments and convert to PaymentResponseDTO
    var paymentDtos =
        paymentRepository.findAll().values().stream()
            .map(Payment::toResponseDto)
            .collect(Collectors.toList());

    // Create a JSON object to wrap the list of payments
    var jsonObject = new JsonObject();
    jsonObject.add("payments", new Gson().toJsonTree(paymentDtos));

    // Convert to JSON string
    String jsonResponse = new Gson().toJson(jsonObject);
    log.info("Returning JSON response: {}", jsonResponse);

    // Set the response content type to application/json
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

    // Send the response
    try {
      exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
      try (var output = exchange.getResponseBody()) {
        output.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      log.error("Exception when getting payments: {}", e.getMessage());
    }
  }

  // Handle POST /payments - Accept JSON request body to create a new payment
  public void handlePostPayment(HttpExchange exchange) {
    try {
      // Read the request body (JSON) to create a new payment
      var requestBody = exchange.getRequestBody();
      var body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
      log.info("Received POST request with body: {}", body);

      // Deserialize the JSON body into a PaymentDTO
      var paymentRequestDTO = gson.fromJson(body, PaymentRequestDTO.class);

      // Delegate to the service to handle payment creation, processing, and response
      processPayment(paymentRequestDTO, exchange);

    } catch (IOException e) {
      log.error("Exception when creating payment: {}", e.getMessage());
    }
  }

  // Handle PATCH /payments/{paymentId} to update the status of an existing payment
  public void handlePatchPayment(HttpExchange exchange) {
    try {

      // Parse the paymentId from the URL (e.g., /payments/{paymentId})
      var pathParts = exchange.getRequestURI().getPath().split("/");
      var paymentId = pathParts[pathParts.length - 1];

      // Read the request body (JSON) for the new status
      var requestBody = exchange.getRequestBody();
      var body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
      log.info("Received PATCH request with body: {}", body);

      // Extract the new status from the request
      var jsonObject = gson.fromJson(body, JsonObject.class);
      var newStatusString = jsonObject.get("status").getAsString();

      // Convert the status to the PaymentStatus enum
      var newStatus = PaymentStatus.valueOf(newStatusString.toUpperCase());

      // Send an immediate response to the client
      var response = "Status update is being processed for Payment ID: " + paymentId;
      exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
      try (var output = exchange.getResponseBody()) {
        output.write(response.getBytes(StandardCharsets.UTF_8));
      }

      // Process the payment status update asynchronously
      executorService.submit(
          () -> {
            try {
              log.info("Processing payment status update for Payment ID: {}", paymentId);

              // Update the payment status in a thread-safe manner
              updatePaymentStatus(paymentId, newStatus);

              log.info("Payment status updated for ID: {} to status: {}", paymentId, newStatus);

              // Optionally publish an event to Kafka
              Payment updatedPayment = paymentRepository.findById(paymentId);
              kafkaProducer.send("payment.status.updated", updatedPayment);

            } catch (Exception e) {
              log.error("Error processing payment status update for ID: {}", paymentId, e);
            }
          });

    } catch (IOException e) {
      log.error("Error handling PATCH request: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("Invalid status value provided: {}", e.getMessage());
      try {
        exchange.sendResponseHeaders(400, -1); // 400 Bad Request
      } catch (IOException ioException) {
        log.error("Error sending 400 response: {}", ioException.getMessage());
      }
    }
  }

  // Process payment and send response back via HttpExchange (for POST /payments)
  private void processPayment(PaymentRequestDTO paymentRequestDTO, HttpExchange exchange) {
    var payment = Payment.fromRequestDTO(paymentRequestDTO); // Convert DTO to domain entity

    // Send a response confirming the payment processing (but processing continues asynchronously)
    String response = "Payment processing started for ID: " + payment.paymentId();
    try {
      exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
      try (OutputStream output = exchange.getResponseBody()) {
        output.write(response.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      log.error("Error sending response for payment creation: {}", e.getMessage());
      return;
    }

    // Process payment asynchronously
    executorService.submit(
        () -> {
          synchronized (payment) { // Synchronize on the individual Payment object
            try {
              log.info("Processing payment: {}", payment.paymentId());

              // Simulate some processing delay
              Thread.sleep(1000);

              // Mark payment as successful and save it
              Payment updatedPayment = payment.markSuccess();
              paymentRepository.save(updatedPayment);

              // Publish success event to Kafka
              kafkaProducer.send("payment.success", updatedPayment);

              // Only log the payment creation after the Kafka producer has successfully sent the
              // message
              log.info(
                  "Payment created and event published for ID: {}", updatedPayment.paymentId());

            } catch (Exception e) {
              log.error("Error processing payment: {}", payment.paymentId(), e);

              // Handle failed payments and publish failure events
              Payment failedPayment = payment.markFailed();
              paymentRepository.save(failedPayment);
              kafkaProducer.send("payment.failed", failedPayment);
            }
          }
        });
  }

  // Modifies an existing payment in a thread-safe manner
  public synchronized void updatePaymentStatus(String paymentId, PaymentStatus newStatus) {

    // Retrieve the payment
    Payment payment = paymentRepository.findById(paymentId);

    if (payment != null) {
      // Modify the payment status
      Payment updatedPayment =
          new Payment(
              payment.paymentId(),
              payment.payerId(),
              payment.recipientId(),
              payment.amount(),
              payment.currency(),
              newStatus);

      // Save the updated payment
      paymentRepository.save(updatedPayment);

      log.info("Payment status updated to: {} for payment ID: {}", newStatus, paymentId);

      // publish an update event to Kafka
      kafkaProducer.send("payment.status.updated", updatedPayment);
    } else {
      log.error("Payment not found for ID: {}", paymentId);
    }
  }
}
