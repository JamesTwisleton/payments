package infrastructure.rest;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;

@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final int port;
  private HttpServer server;

  public PaymentController(PaymentService paymentService, Properties config) {
    this.paymentService = paymentService;
    this.port = Integer.parseInt(config.getProperty("port"));
  }

  public void startServer() throws Exception {

    // Create server listening on specified port, allow up to 100 queued requests
    server = HttpServer.create(new InetSocketAddress(port), 100);

    server.createContext(
        "/transactions",
        exchange -> {
          switch (exchange.getRequestMethod()) {
            case "POST":
              paymentService.handlePostTransaction(exchange);
              return;
            case "GET":
              paymentService.handleGetTransactions(exchange);
              return;
            default:
              exchange.sendResponseHeaders(405, -1);
          }
        });

    server.createContext("/accounts", paymentService::handleGetAccount);

    // Handle requests concurrently with a thread pool executor
    server.setExecutor(Executors.newFixedThreadPool(10)); // Thread pool with 10 threads
    server.start();
    log.info("Payments server started on port {}", port);
  }

  public void stopServer() {
    if (server != null) {
      server.stop(0);
      log.info("Payments server stopped.");
    }
  }
}
