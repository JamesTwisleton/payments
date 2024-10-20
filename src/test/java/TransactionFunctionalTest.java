import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

public class TransactionFunctionalTest extends BaseFunctionalTest {

  @Test
  public void transaction_success() {
    String transactionRequestJson =
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
  public void transaction_insufficientBalance() {
    String transactionRequestJson =
        """
        {
            "senderId": "ACCOUNT_ID_0",
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
        .statusCode(400)
        .body(containsString("Insufficient balance in sender account ACCOUNT_ID_0"));
  }
}
