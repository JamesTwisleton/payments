package domain.repository;

import domain.entity.Payment;
import domain.entity.PaymentStatus;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {
  private final Map<String, Payment> payments = new ConcurrentHashMap<>();

  @Override
  public void save(Payment payment) {
    payments.put(payment.paymentId(), payment);
  }

  @Override
  public Payment findById(String paymentId) {
    return payments.get(paymentId);
  }

  @Override
  public Map<String, Payment> findAll() {
    return payments;
  }

  // Modifies an existing payment in a thread safe manner
  public synchronized void updatePaymentStatus(String paymentId, PaymentStatus newStatus) {

    // Retrieve the payment
    Payment payment = findById(paymentId);

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
      save(updatedPayment);
    }
  }
}
