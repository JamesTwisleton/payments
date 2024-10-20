import domain.repository.InMemoryAccountRepository;
import domain.repository.InMemoryTransactionRepository;
import infrastructure.rest.PaymentController;
import io.restassured.RestAssured;
import java.net.ServerSocket;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import service.TransactionService;

public abstract class BaseFunctionalTest {

  protected static PaymentController paymentController;

  @BeforeAll
  public static void setUp() throws Exception {
    int randomPort = getRandomPort();
    Properties config = new Properties();
    config.setProperty("port", String.valueOf(randomPort));
    InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
    InMemoryTransactionRepository transactionRepository = new InMemoryTransactionRepository();
    TransactionService transactionService =
        new TransactionService(accountRepository, transactionRepository);

    // Start the server
    paymentController = new PaymentController(transactionService, config);
    paymentController.startServer();

    // Set the base URI for RestAssured
    RestAssured.baseURI = "http://localhost:" + randomPort;
  }

  @AfterAll
  public static void tearDown() {
    if (paymentController != null) {
      paymentController.stopServer();
    }
  }

  private static int getRandomPort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    }
  }
}
