package domain.repository;

import domain.entity.Payment;
import java.util.Map;

public interface PaymentRepository {
  void save(Payment payment);

  Payment findById(String paymentId);

  Map<String, Payment> findAll();
}
