package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class BoletoPayloadInvalidoTest {

  @Test
  void deveRetornar400QuandoJsonMalformado() {
    given()
      .contentType(ContentType.JSON)
      .body("{" )
      .when().post("/boletos")
      .then()
      .statusCode(400)
      .body("status", equalTo(400))
      .body("codigo", equalTo("BAD_REQUEST"))
      .body("mensagem", notNullValue());
  }
}
