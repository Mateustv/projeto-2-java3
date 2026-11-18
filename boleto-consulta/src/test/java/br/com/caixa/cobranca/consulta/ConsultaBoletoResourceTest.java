package br.com.caixa.cobranca.consulta;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import br.com.caixa.cobranca.consulta.security.SecurityRoles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class ConsultaBoletoResourceTest {

  @Test
  void deveConsultarCom200() {
    given()
      .when().get("/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(200)
      .body("status", equalTo("AGUARDANDO_REGISTRO"));
  }

  @Test
  void deveRetornar400ParaIdentificadorInvalido() {
    given()
      .when().get("/boletos/abc")
      .then()
      .statusCode(400)
      .body("status", equalTo(400))
      .body("codigo", equalTo("BAD_REQUEST"));
  }

  @Test
  void deveRetornar404QuandoBoletoNaoEncontrado() {
    given()
      .when().get("/boletos/22222222-2222-2222-2222-222222222222")
      .then()
      .statusCode(404)
      .body("status", equalTo(404))
      .body("codigo", equalTo("NOT_FOUND"));
  }
}
