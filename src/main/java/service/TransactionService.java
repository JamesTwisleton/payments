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
public class TransactionService {

  public void HandlePostTransaction(HttpExchange exchange) {
    log.info("Got to transaction service!");
    // Send the response
    try {
      // Convert to JSON string
      String jsonResponse = new Gson().toJson("Transaction received");
      exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
      try (var output = exchange.getResponseBody()) {
        output.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      log.error("Exception when submitting transaction: {}", e.getMessage());
    }
  }

}
