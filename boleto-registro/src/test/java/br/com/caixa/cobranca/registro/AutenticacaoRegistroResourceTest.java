package br.com.caixa.cobranca.registro;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AutenticacaoRegistroResourceTest {

  @Test
  void postSemCredencialRetorna401ComDesafioBasic() {
    given()
      .contentType(ContentType.JSON)
      .body(payloadComIdentificador(UUID.randomUUID()))
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(401)
      .header("WWW-Authenticate", containsStringIgnoringCase("Basic"));
  }

  @Test
  void getSemCredencialRetorna401ComDesafioBasic() {
    given()
      .when().get("/interno/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(401)
      .header("WWW-Authenticate", containsStringIgnoringCase("Basic"));
  }

  @Test
  void deleteSemCredencialRetorna401ComDesafioBasic() {
    given()
      .when().delete("/interno/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(401)
      .header("WWW-Authenticate", containsStringIgnoringCase("Basic"));
  }

  @Test
  void auditorNaoPodePostNemDelete() {
    Response post = given()
      .auth().preemptive().basic("auditor", "auditor123")
      .contentType(ContentType.JSON)
      .body(payloadComIdentificador(UUID.randomUUID()))
      .when().post("/interno/comandos/registro");
    assertForbidden(post);

    Response delete = given()
      .auth().preemptive().basic("auditor", "auditor123")
      .when().delete("/interno/boletos/11111111-1111-1111-1111-111111111111");
    assertForbidden(delete);
  }

  @Test
  void auditorPodeGetMesmoQuandoNaoExisteRegistro() {
    given()
      .auth().preemptive().basic("auditor", "auditor123")
      .when().get("/interno/boletos/22222222-2222-2222-2222-222222222222")
      .then()
      .statusCode(404)
      .body("status", equalTo(404))
      .body("codigo", equalTo("NOT_FOUND"));
  }

  @Test
  void operadorPodePostGetDeleteNoFluxoCompleto() {
    UUID id = UUID.randomUUID();
    String payload = payloadComIdentificador(id);

    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body(payload)
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(201)
      .body("status", equalTo("REGISTRADO"));

    given()
      .auth().preemptive().basic("operador", "operador123")
      .when().get("/interno/boletos/" + id)
      .then()
      .statusCode(200)
      .body("identificador", equalTo(id.toString()));

    given()
      .auth().preemptive().basic("operador", "operador123")
      .when().delete("/interno/boletos/" + id)
      .then()
      .statusCode(204);
  }

  private static void assertForbidden(Response resposta) {
    assertEquals(403, resposta.statusCode());
    String contentType = resposta.getContentType();
    if (contentType != null && contentType.contains("application/json")) {
      resposta.then()
        .body("status", equalTo(403))
        .body("codigo", equalTo("FORBIDDEN"))
        .body("mensagem", equalTo("acesso negado"));
    }
  }

  private static String payloadComIdentificador(UUID identificador) {
    return """
      {
        "identificador": "%s"
      }
      """.formatted(identificador);
  }
}
