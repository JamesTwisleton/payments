import config.ConfigLoader;
import domain.repository.InMemoryAccountRepository;
import domain.repository.InMemoryPaymentRepository;
import domain.repository.InMemoryTransactionRepository;
import infrastructure.messaging.KafkaProducer;
import infrastructure.rest.PaymentController;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;
import service.TransactionService;

import java.util.Properties;

@Slf4j
public class Payments {
  public static void main(String[] args) throws Exception {
    // Manual Dependency Injection
    Properties config = ConfigLoader.loadProperties();
    InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
    KafkaProducer kafkaProducer = new KafkaProducer();
    PaymentService paymentService = new PaymentService(paymentRepository, kafkaProducer);
    InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
    InMemoryTransactionRepository transactionRepository = new InMemoryTransactionRepository();
    TransactionService transactionService = new TransactionService(accountRepository, transactionRepository);
    PaymentController controller = new PaymentController(paymentService, transactionService, config);

    // Start the server
    controller.startServer();
  }
}
