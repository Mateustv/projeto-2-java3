package br.com.caixa.cobranca.registro;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StatusResourceTest {

  @Test
  void deveResponderStatusDoServico() {
    given()
      .when().get("/status")
      .then()
      .statusCode(200)
      .body("servico", is("boleto-registro"))
      .body("status", is("UP"));
  }
}
