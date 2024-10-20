package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import domain.entity.Account;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentUtils {

  private static final Gson gson = new Gson();

  // Acquires locks in a fixed order based on account ID to prevent deadlock
  public static void acquireLocks(Account account1, Account account2) {
    Lock firstLock, secondLock;
    if (account1.getAccountId().compareTo(account2.getAccountId()) < 0) {
      firstLock = account1.getLock();
      secondLock = account2.getLock();
    } else {
      firstLock = account2.getLock();
      secondLock = account1.getLock();
    }
    firstLock.lock();
    secondLock.lock();
  }

  // Releases locks in reverse order of acquisition
  public static void releaseLocks(Account account1, Account account2) {
    Lock firstLock, secondLock;
    if (account1.getAccountId().compareTo(account2.getAccountId()) < 0) {
      firstLock = account1.getLock();
      secondLock = account2.getLock();
    } else {
      firstLock = account2.getLock();
      secondLock = account1.getLock();
    }
    secondLock.unlock();
    firstLock.unlock();
  }

  // Sends a JSON response (always expects a valid JSON object)
  public static void sendResponse(
      HttpExchange exchange, int responseCode, JsonObject responseBody) {
    try {
      String jsonResponse = gson.toJson(responseBody);
      exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
      exchange.sendResponseHeaders(
          responseCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
      try (var output = exchange.getResponseBody()) {
        output.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      log.error("Error sending response: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  // Generic method to get the request body as a specified type and handle errors
  public static <T> T getRequestBodyAsType(HttpExchange exchange, Class<T> type) {
    try {
      var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      log.info("Received POST request with body: {}", body);

      // Deserialize the JSON body into the specified type
      return gson.fromJson(body, type);
    } catch (Exception e) {
      log.error("Failed to read request body: {}", e.getMessage(), e);

      // Send a valid JSON object with an error message
      var errorResponse = new JsonObject();
      errorResponse.addProperty("error", "Failed to parse request body");
      sendResponse(exchange, 400, errorResponse);

      return null;
    }
  }
}
