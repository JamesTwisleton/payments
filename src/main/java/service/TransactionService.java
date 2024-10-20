package service;

import com.sun.net.httpserver.HttpExchange;
import domain.dto.TransactionRequestDTO;
import domain.entity.Account;
import domain.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import util.PaymentUtils;

@Slf4j
public class TransactionService {

  private final AccountRepository accountRepository;

  public TransactionService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public void handlePostTransaction(HttpExchange exchange) {
    TransactionRequestDTO transactionRequestDto =
        PaymentUtils.getRequestBodyAsType(exchange, TransactionRequestDTO.class);

    if (transactionRequestDto == null) {
      return; // Invalid request body, response already sent in getRequestBodyAsType
    }

    String senderId = transactionRequestDto.senderId();
    String recipientId = transactionRequestDto.recipientId();
    var amount = transactionRequestDto.amount();

    var maybeSenderAccount = accountRepository.findByAccountId(senderId);
    var maybeRecipientAccount = accountRepository.findByAccountId(recipientId);

    if (maybeSenderAccount.isEmpty()) {
      PaymentUtils.sendResponse(
          exchange, 404, String.format("Invalid sender account ID: %s", senderId));
      return;
    }

    if (maybeRecipientAccount.isEmpty()) {
      PaymentUtils.sendResponse(
          exchange, 404, String.format("Invalid recipient account ID: %s", recipientId));
      return;
    }

    Account senderAccount = maybeSenderAccount.get();
    Account recipientAccount = maybeRecipientAccount.get();

    // Acquire locks using utility method
    PaymentUtils.acquireLocks(senderAccount, recipientAccount);

    try {
      if (senderAccount.getBalance().compareTo(amount) < 0) {
        PaymentUtils.sendResponse(
            exchange, 400, String.format("Insufficient balance in sender account %s", senderId));
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

      PaymentUtils.sendResponse(exchange, 200, "Transaction successful!");

    } finally {
      // Release locks using utility method
      PaymentUtils.releaseLocks(senderAccount, recipientAccount);
    }
  }
}
