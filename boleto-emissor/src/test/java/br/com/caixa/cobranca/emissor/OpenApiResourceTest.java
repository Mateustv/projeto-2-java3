package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OpenApiResourceTest {

  @Test
  void devePublicarOpenApi() {
    given()
      .when().get("/q/openapi")
      .then()
      .statusCode(200)
      .body(containsString("openapi"));
  }
}
