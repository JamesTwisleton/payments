import config.ConfigLoader;
import domain.repository.InMemoryAccountRepository;
import domain.repository.InMemoryTransactionRepository;
import infrastructure.rest.PaymentController;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;

@Slf4j
public class Payments {
  public static void main(String[] args) throws Exception {
    // Manual Dependency Injection
    var config = ConfigLoader.loadProperties();
    var accountRepository = new InMemoryAccountRepository();
    var transactionRepository = new InMemoryTransactionRepository();
    var transactionService = new PaymentService(accountRepository, transactionRepository);
    PaymentController controller = new PaymentController(transactionService, config);

    // Start the server
    controller.startServer();
  }
}
