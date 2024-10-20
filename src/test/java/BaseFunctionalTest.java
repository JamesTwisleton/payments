import domain.repository.InMemoryAccountRepository;
import domain.repository.InMemoryTransactionRepository;
import infrastructure.rest.PaymentController;
import io.restassured.RestAssured;
import java.net.ServerSocket;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import service.PaymentService;

public abstract class BaseFunctionalTest {

  protected static PaymentController paymentController;
  protected static InMemoryAccountRepository accountRepository;
  protected static InMemoryTransactionRepository transactionRepository;

  @BeforeEach
  public void beforeEach() throws Exception {
    int randomPort = getRandomPort();
    Properties config = new Properties();
    config.setProperty("port", String.valueOf(randomPort));
    accountRepository = new InMemoryAccountRepository();
    transactionRepository = new InMemoryTransactionRepository();
    PaymentService paymentService = new PaymentService(accountRepository, transactionRepository);

    // Start the server
    paymentController = new PaymentController(paymentService, config);
    paymentController.startServer();

    // Set the base URI for RestAssured
    RestAssured.baseURI = "http://localhost:" + randomPort;
  }

  @AfterAll
  public static void tearDown() {
    if (paymentController != null) {
      paymentController.stopServer();
    }
    accountRepository = null;
    transactionRepository = null;

  }

  private static int getRandomPort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    }
  }
}
