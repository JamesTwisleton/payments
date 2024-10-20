package infrastructure.rest;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;
import service.TransactionService;

@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final TransactionService transactionService;
  private final int port;

  public PaymentController(PaymentService paymentService, TransactionService transactionService, Properties config) {
    this.paymentService = paymentService;
    this.transactionService = transactionService;
    this.port = Integer.parseInt(config.getProperty("port"));
  }

  public void startServer() throws Exception {

    // Create server listening on specified port, allow up to 100 queued requests
    var server = HttpServer.create(new InetSocketAddress(port), 100);

    // VERSION 0
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

    // VERSION 1
    server.createContext(
        "/transactions",
        exchange -> {
          switch (exchange.getRequestMethod()) {
            case "POST":
              transactionService.handlePostTransaction(exchange);
            default:
              exchange.sendResponseHeaders(405, -1);
          }
        });

    // Handle requests concurrently with a thread pool executor
    server.setExecutor(Executors.newFixedThreadPool(10)); // Thread pool with 10 threads
    server.start();
    log.info("Payments server started on port {}", port);
  }
}
