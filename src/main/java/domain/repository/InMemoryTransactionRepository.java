package domain.repository;

import domain.entity.Payment;
import domain.entity.Transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTransactionRepository implements TransactionRepository {
  private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

  @Override
  public void save(Transaction transaction) {
    transactions.put(transaction.transactionId(), transaction);
  }

  @Override
  public Transaction findById(String paymentId) {
    return transactions.get(paymentId);
  }

  @Override
  public Map<String, Transaction> findAll() {
    return transactions;
  }
}
