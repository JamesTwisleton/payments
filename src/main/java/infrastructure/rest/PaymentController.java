package infrastructure.rest;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;
import service.TransactionService;

@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final TransactionService transactionService;

  public PaymentController(PaymentService paymentService, TransactionService transactionService) {
    this.paymentService = paymentService;
    this.transactionService = transactionService;
  }

  public void startServer() throws Exception {

    // Create server listening on port 8000, allow up to 100 queued requests
    var server = HttpServer.create(new InetSocketAddress(8000), 100);

    // listen on /payments
    server.createContext(
        "/payments",
        exchange -> {
          switch (exchange.getRequestMethod()) {
            case "GET":
              paymentService.handleGetPayments(exchange);
            case "POST":
              paymentService.handlePostPayment(exchange);
            case "PATCH":
              paymentService.handlePatchPayment(exchange);
            default:
              exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
          }
        });

    server.createContext(
        "/transactions",
        exchange -> {
          switch (exchange.getRequestMethod()) {
            case "POST":
              transactionService.HandlePostTransaction(exchange);
            default:
              exchange.sendResponseHeaders(405, -1);
          }
        });

    // Handle requests concurrently with a thread pool executor
    server.setExecutor(Executors.newFixedThreadPool(10)); // Thread pool with 10 threads
    server.start();
    log.info("Server started on port 8000");
  }
}
