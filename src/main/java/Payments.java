import domain.repository.InMemoryPaymentRepository;
import infrastructure.messaging.KafkaProducer;
import infrastructure.rest.PaymentController;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;

@Slf4j
public class Payments {
  public static void main(String[] args) throws Exception {
    // Manual Dependency Injection
    InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
    KafkaProducer kafkaProducer = new KafkaProducer();
    PaymentService paymentService = new PaymentService(paymentRepository, kafkaProducer);
    PaymentController controller = new PaymentController(paymentService);

    // Start the server
    controller.startServer();
  }
}
