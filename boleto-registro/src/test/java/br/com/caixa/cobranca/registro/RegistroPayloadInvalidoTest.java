package br.com.caixa.cobranca.registro;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RegistroPayloadInvalidoTest {

  @Test
  void deveRetornar400QuandoJsonMalformado() {
    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body("{")
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(400)
      .body("status", equalTo(400))
      .body("codigo", equalTo("BAD_REQUEST"));
  }
}
