package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import domain.dto.TransactionRequestDTO;
import domain.entity.Account;
import domain.entity.Transaction;
import domain.entity.TransactionStatus;
import domain.repository.AccountRepository;
import domain.repository.TransactionRepository;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import util.Utils;

@Slf4j
public class TransactionService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public TransactionService(
      AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  public void handlePostTransaction(HttpExchange exchange) {
    TransactionRequestDTO transactionRequestDto =
        Utils.getRequestBodyAsType(exchange, TransactionRequestDTO.class);

    if (transactionRequestDto == null) {
      return; // Invalid request body, response already sent in getRequestBodyAsType
    }

    String senderId = transactionRequestDto.senderId();
    String recipientId = transactionRequestDto.recipientId();
    var amount = transactionRequestDto.amount();

    var maybeSenderAccount = accountRepository.findByAccountId(senderId);
    var maybeRecipientAccount = accountRepository.findByAccountId(recipientId);
    var transactionBuilder =
        Transaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .senderId(senderId)
            .recipientId(recipientId)
            .amount(amount);
    var jsonResponse = new JsonObject();

    if (maybeSenderAccount.isEmpty()) {
      jsonResponse.addProperty("error", String.format("Invalid sender account ID: %s", senderId));
      Utils.sendResponse(exchange, 404, jsonResponse);
      transactionRepository.save(
          transactionBuilder.status(TransactionStatus.SENDER_NOT_FOUND).build());
      return;
    }

    if (maybeRecipientAccount.isEmpty()) {
      jsonResponse.addProperty(
          "error", String.format("Invalid recipient account ID: %s", recipientId));
      Utils.sendResponse(exchange, 404, jsonResponse);
      transactionRepository.save(
          transactionBuilder.status(TransactionStatus.RECIPIENT_NOT_FOUND).build());
      return;
    }

    Account senderAccount = maybeSenderAccount.get();
    Account recipientAccount = maybeRecipientAccount.get();

    Utils.acquireLocks(senderAccount, recipientAccount);

    try {
      if (senderAccount.getBalance().compareTo(amount) < 0) {
        jsonResponse.addProperty(
            "error", String.format("Insufficient balance in sender account %s", senderId));
        Utils.sendResponse(exchange, 400, jsonResponse);
        transactionRepository.save(
            transactionBuilder.status(TransactionStatus.INSUFFICIENT_BALANCE).build());
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

      transactionRepository.save(transactionBuilder.status(TransactionStatus.SUCCESS).build());

      var successResponse = new JsonObject();
      successResponse.addProperty("message", "Transaction successful!");
      Utils.sendResponse(exchange, 200, successResponse);

    } finally {
      Utils.releaseLocks(senderAccount, recipientAccount);
    }
  }

  public void handleGetTransactions(HttpExchange exchange) {
    var transactionDtos =
        transactionRepository.findAll().values().stream()
            .map(Transaction::toResponseDto)
            .collect(Collectors.toList());

    var jsonObject = new JsonObject();
    jsonObject.add("transactions", new Gson().toJsonTree(transactionDtos));

    Utils.sendResponse(exchange, 200, jsonObject);
  }
}
