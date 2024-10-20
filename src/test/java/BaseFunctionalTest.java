import domain.repository.InMemoryAccountRepository;
import domain.repository.InMemoryPaymentRepository;
import domain.repository.InMemoryTransactionRepository;
import infrastructure.messaging.KafkaProducer;
import infrastructure.rest.PaymentController;
import io.restassured.RestAssured;
import java.net.ServerSocket;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import service.PaymentService;
import service.TransactionService;

public abstract class BaseFunctionalTest {

  protected static PaymentController paymentController;

  @BeforeAll
  public static void setUp() throws Exception {
    // Initialize repositories and services
    int randomPort = getRandomPort();
    Properties config = new Properties();
    config.setProperty("port", String.valueOf(randomPort));

    InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();
    InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
    InMemoryTransactionRepository transactionRepository = new InMemoryTransactionRepository();
    KafkaProducer kafkaProducer = new KafkaProducer();

    PaymentService paymentService = new PaymentService(paymentRepository, kafkaProducer);
    TransactionService transactionService = new TransactionService(accountRepository, transactionRepository);

    // Start the server
    paymentController = new PaymentController(paymentService, transactionService, config);
    paymentController.startServer();

    // Set the base URI for RestAssured
    RestAssured.baseURI = "http://localhost:" + randomPort;
  }

  @AfterAll
  public static void tearDown() {
    // Stop the server after tests
    if (paymentController != null) {
      paymentController.stopServer();
    }
  }

  // Helper method to get an available random port
  private static int getRandomPort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    }
  }
}
