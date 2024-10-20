package domain.repository;

import domain.entity.Transaction;
import java.util.Map;

public interface TransactionRepository {
  void save(Transaction transaction);

  Transaction findById(String transacrtionId);

  Map<String, Transaction> findAll();
}
