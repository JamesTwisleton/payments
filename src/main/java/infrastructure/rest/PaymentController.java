package infrastructure.rest;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import service.PaymentService;

@Slf4j
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  public void startServer() throws Exception {

    // Create server listening on port 8000, allow up to 100 queued requests
    var server = HttpServer.create(new InetSocketAddress(8000), 100);

    // listen on /payments
    server.createContext(
        "/payments",
        exchange -> {
          // Handle GET /payments to get all existing payments
          if ("GET".equals(exchange.getRequestMethod())) {
            paymentService.handleGetPayments(exchange);

            // Handle POST /payments to submit a new payment
          } else if ("POST".equals(exchange.getRequestMethod())) {
            paymentService.handlePostPayment(exchange);

            // Handle PATCH /payments/{paymentId} to update the status of a payment
          } else if ("PATCH".equals(exchange.getRequestMethod())) {
            paymentService.handlePatchPayment(exchange);

          } else {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
          }
        });

    // Handle requests concurrently with a thread pool executor
    server.setExecutor(Executors.newFixedThreadPool(10)); // Thread pool with 10 threads
    server.start();
    log.info("Server started on port 8000");
  }
}
