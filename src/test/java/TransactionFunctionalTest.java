import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

public class TransactionFunctionalTest extends BaseFunctionalTest {

  @Test
  @Description("Create transaction - success")
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
  @Description("Create transaction - insufficient balance")
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

  @Test
  @Description("Create transaction - sender not found")
  public void transaction_senderNotFound() {
    String transactionRequestJson =
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
    String transactionRequestJson =
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
}
