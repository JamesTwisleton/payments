import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jdk.jfr.Description;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TransactionFunctionalTest extends BaseFunctionalTest {

  @Test
  @Description("Create transaction - success")
  public void transaction_success() {
    var transactionRequestJson =
        """
        {
            "senderId": "ACCOUNT_ID_0",
            "recipientId": "ACCOUNT_ID_1",
            "amount": 50.00
        }
        """;

    given()
        .header("Content-Type", "application/json")
        .body(transactionRequestJson)
        .when()
        .post("/transactions")
        .then()
        .statusCode(200)
        .body(containsString("Transaction successful!"));
  }

  @Test
  @Description("Create transaction - insufficient balance")
  public void transaction_insufficientBalance() {
    var transactionRequestJson =
        """
        {
            "senderId": "ACCOUNT_ID_5",
            "recipientId": "ACCOUNT_ID_1",
            "amount": 1.00
        }
        """;

    given()
        .header("Content-Type", "application/json")
        .body(transactionRequestJson)
        .when()
        .post("/transactions")
        .then()
        .statusCode(400)
        .body(containsString("Insufficient balance in sender account ACCOUNT_ID_5"));
  }

  @Test
  @Description("Create transaction - sender not found")
  public void transaction_senderNotFound() {
    var transactionRequestJson =
        """
            {
                "senderId": "ACCOUNT_ID_69",
                "recipientId": "ACCOUNT_ID_1",
                "amount": 10000.00
            }
            """;

    given()
        .header("Content-Type", "application/json")
        .body(transactionRequestJson)
        .when()
        .post("/transactions")
        .then()
        .statusCode(404)
        .body(containsString("Invalid sender account ID: ACCOUNT_ID_69"));
  }

  @Test
  @Description("Create transaction - recipient not found")
  public void transaction_recipientNotFound() {
    var transactionRequestJson =
        """
            {
                "senderId": "ACCOUNT_ID_1",
                "recipientId": "ACCOUNT_ID_420",
                "amount": 10000.00
            }
            """;

    given()
        .header("Content-Type", "application/json")
        .body(transactionRequestJson)
        .when()
        .post("/transactions")
        .then()
        .statusCode(404)
        .body(containsString("Invalid recipient account ID: ACCOUNT_ID_420"));
  }

  @Test
  @Description("Empty accounts with 100 balance and fill accounts with 0 balance")
  public void testEmptyFullAccountsAndFillEmptyAccounts() throws InterruptedException {
    // Executor to simulate concurrent transactions
    ExecutorService executor = Executors.newFixedThreadPool(5); // One thread per full account

    List<String> fullAccounts =
        new ArrayList<>(
            List.of(
                "ACCOUNT_ID_0", "ACCOUNT_ID_1", "ACCOUNT_ID_2", "ACCOUNT_ID_3", "ACCOUNT_ID_4"));
    List<String> emptyAccounts =
        new ArrayList<>(
            List.of(
                "ACCOUNT_ID_5", "ACCOUNT_ID_6", "ACCOUNT_ID_7", "ACCOUNT_ID_8", "ACCOUNT_ID_9"));

    // Shuffle fullAccounts and emptyAccounts before running transactions
    Collections.shuffle(fullAccounts);
    Collections.shuffle(emptyAccounts);

    // Amount to transfer per transaction (equal amounts to fully empty the account)
    BigDecimal totalTransferAmount = new BigDecimal("100.00");
    BigDecimal transferPerTransaction =
        totalTransferAmount.divide(BigDecimal.valueOf(emptyAccounts.size()));

    // Transfer all funds from each full account to all empty accounts
    for (String senderAccountId : fullAccounts) {
      for (String recipientAccountId : emptyAccounts) {
        Runnable task =
            () -> {
              var transactionRequestJson =
                  """
                  {
                      "senderId": "%s",
                      "recipientId": "%s",
                      "amount": %s
                  }
                  """
                      .formatted(
                          senderAccountId, recipientAccountId, transferPerTransaction.toString());

              given()
                  .header("Content-Type", "application/json")
                  .body(transactionRequestJson)
                  .when()
                  .post("/transactions")
                  .then()
                  .statusCode(
                      anyOf(
                          is(200), is(400),
                          is(404))); // Handle success, insufficient balance, or invalid accounts
            };

        // Submit the transaction task to the executor
        executor.submit(task);
      }
    }

    // Shutdown the executor and wait for all tasks to complete
    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS); // 30 seconds timeout for the test to finish

    // Verify the final balances of full and empty accounts
    verifyFinalBalances();
  }

  private void verifyFinalBalances() {
    // Query the final balances for the full accounts (should all be 4.00)
    Map<String, BigDecimal> fullAccountBalances =
        Map.of(
            "ACCOUNT_ID_0", getAccountBalance("ACCOUNT_ID_0"),
            "ACCOUNT_ID_1", getAccountBalance("ACCOUNT_ID_1"),
            "ACCOUNT_ID_2", getAccountBalance("ACCOUNT_ID_2"),
            "ACCOUNT_ID_3", getAccountBalance("ACCOUNT_ID_3"),
            "ACCOUNT_ID_4", getAccountBalance("ACCOUNT_ID_4"));

    // Query the final balances for the empty accounts (should all be 100.00)
    Map<String, BigDecimal> emptyAccountBalances =
        Map.of(
            "ACCOUNT_ID_5", getAccountBalance("ACCOUNT_ID_5"),
            "ACCOUNT_ID_6", getAccountBalance("ACCOUNT_ID_6"),
            "ACCOUNT_ID_7", getAccountBalance("ACCOUNT_ID_7"),
            "ACCOUNT_ID_8", getAccountBalance("ACCOUNT_ID_8"),
            "ACCOUNT_ID_9", getAccountBalance("ACCOUNT_ID_9"));

    // Print final balances (for debugging purposes)
    log.info("Final full account balances (should be 0.00): {}", fullAccountBalances);
    log.info("Final empty account balances (should be 100.00): {}", emptyAccountBalances);

    // Assert all full accounts are left with 0.00
    fullAccountBalances.values().forEach(balance -> assertEquals(new BigDecimal("0.00"), balance));

    // Assert all empty accounts are filled with 100.00
    emptyAccountBalances
        .values()
        .forEach(balance -> assertEquals(new BigDecimal("100.00"), balance));
  }

  private BigDecimal getAccountBalance(String accountId) {
    // Send a GET request to the API to retrieve the account details
    var response =
        given()
            .header("Content-Type", "application/json")
            .when()
            .get("/accounts/" + accountId)
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    // Extract the balance from the nested "account" object in the JSON response
    String balanceString = response.getString("account.balance");
    return new BigDecimal(balanceString);
  }
}
