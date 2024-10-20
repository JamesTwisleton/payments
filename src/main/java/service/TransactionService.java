package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import domain.dto.PaymentRequestDTO;
import domain.dto.TransactionRequestDTO;
import domain.entity.Account;
import domain.entity.Payment;
import domain.entity.PaymentStatus;
import domain.repository.AccountRepository;
import domain.repository.PaymentRepository;
import infrastructure.messaging.KafkaProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionService {

  private final Gson gson = new Gson();
  private final AccountRepository accountRepository;

  public TransactionService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public void HandlePostTransaction(HttpExchange exchange) {
    var transactionRequestDto = getTransactionRequestDtoFromRequest(exchange);
    var senderId = transactionRequestDto.senderId();
    var recipientId = transactionRequestDto.recipientId();
    var amount = transactionRequestDto.amount();

    var maybeSenderAccount = accountRepository.findByAccountId(senderId);
    var maybeRecipientAccount = accountRepository.findByAccountId(recipientId);

    if (maybeSenderAccount.isEmpty()) {
      sendResponse(exchange, 404, String.format("Invalid sender account ID: %s", senderId));
    }

    if (maybeRecipientAccount.isEmpty()) {
      sendResponse(exchange, 404, String.format("Invalid recipient account ID: %s", senderId));
    }

    Account senderAccount = maybeSenderAccount.get();
    Account recipientAccount = maybeRecipientAccount.get();

    Account firstLock, secondLock;
    if (senderId.compareTo(recipientId) < 0) {
      firstLock = senderAccount;
      secondLock = recipientAccount;
    } else {
      firstLock = recipientAccount;
      secondLock = senderAccount;
    }

    firstLock.lock();
    secondLock.lock();

    try {
      if (senderAccount.getBalance().compareTo(amount) < 0) {
        sendResponse(
            exchange, 400, String.format("Insufficient balance in sender account %s", senderId));
        log.error(
            "Transaction not successful from {} to {} for amount {} - insufficient balance in sender account",
            senderId,
            recipientId,
            amount);
        return;
      }

      // Perform the balance transfer
      senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
      recipientAccount.setBalance(recipientAccount.getBalance().add(amount));
      log.info("Transaction successful from {} to {} for amount {}", senderId, recipientId, amount);
      log.info(
          "Account {} now has balance {}, Account {} now has balance {}",
          senderId,
          senderAccount.getBalance(),
          recipientId,
          recipientAccount.getBalance());
      sendResponse(exchange, 200, "Transaction successful!");
    } finally {
      secondLock.unlock();
      firstLock.unlock();
    }
  }

  private TransactionRequestDTO getTransactionRequestDtoFromRequest(HttpExchange exchange) {
    try {
      var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      log.info("Received POST request to transactions endpoint with body: {}", body);

      // Deserialize the JSON body into TransactionRequestDTO
      return gson.fromJson(body, TransactionRequestDTO.class);
    } catch (Exception e) {
      log.error("Failed to read request body: {}", e.getMessage(), e);
      sendResponse(exchange, 400, "Failed to parse request body");
      throw new IllegalArgumentException("Failed to parse request body");
    }
  }

  private void sendResponse(HttpExchange exchange, int responseCode, Object responseBody) {
    try {
      String jsonResponse = new Gson().toJson(responseBody);
      exchange.sendResponseHeaders(
          responseCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
      var output = exchange.getResponseBody();
      output.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
