import domain.repository.InMemoryPaymentRepository;
import infrastructure.messaging.KafkaProducer;
import infrastructure.rest.PaymentController;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;
import service.TransactionService;

@Slf4j
public class Payments {
  public static void main(String[] args) throws Exception {
    // Manual Dependency Injection
    InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
    KafkaProducer kafkaProducer = new KafkaProducer();
    PaymentService paymentService = new PaymentService(paymentRepository, kafkaProducer);
    TransactionService transactionService = new TransactionService();
    PaymentController controller = new PaymentController(paymentService, transactionService);

    // Start the server
    controller.startServer();
  }
}
