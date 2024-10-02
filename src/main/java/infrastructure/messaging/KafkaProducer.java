package infrastructure.messaging;

import domain.entity.Payment;
import lombok.extern.slf4j.Slf4j;

// TODO: replace with actual in memory kafka
@Slf4j
public class KafkaProducer {

  // Simulate sending an event to Kafka
  public void send(String topic, Payment payment) {
    log.info(
        "Published event to {}: Payment ID = {}, Status = {}",
        topic,
        payment.paymentId(),
        payment.status());
  }
}
