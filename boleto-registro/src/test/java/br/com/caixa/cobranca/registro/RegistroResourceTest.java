package br.com.caixa.cobranca.registro;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RegistroResourceTest {

  @Test
  void deveRegistrarCom201() {
    UUID id = UUID.randomUUID();

    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body("""
        {
          "identificador": "%s"
        }
        """.formatted(id))
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(201)
      .body("status", equalTo("REGISTRADO"));
  }

  @Test
  void deveRetornar200QuandoDuplicado() {
    UUID id = UUID.randomUUID();
    String body = """
      {
        "identificador": "%s"
      }
      """.formatted(id);

    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body(body)
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(201);

    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body(body)
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(200);
  }

  @Test
  void deveRetornar400ParaComandoInvalido() {
    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body("{}")
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(400)
      .body("status", equalTo(400))
      .body("codigo", equalTo("BAD_REQUEST"))
      .body("mensagem", containsString("identificador e obrigatorio"));
  }

  @Test
  void deveRetornar404QuandoNaoEncontrarConsultaInterna() {
    given()
      .auth().preemptive().basic("auditor", "auditor123")
      .when().get("/interno/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(404)
      .body("status", equalTo(404))
      .body("codigo", equalTo("NOT_FOUND"));
  }

  @Test
  void deveRemoverCom204() {
    UUID id = UUID.randomUUID();
    String body = """
      {
        "identificador": "%s"
      }
      """.formatted(id);

    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body(body)
      .when().post("/interno/comandos/registro")
      .then()
      .statusCode(201);

    given()
      .auth().preemptive().basic("operador", "operador123")
      .when().delete("/interno/boletos/" + id)
      .then()
      .statusCode(204);
  }

  @Test
  void deveRetornar404AoRemoverInexistente() {
    given()
      .auth().preemptive().basic("operador", "operador123")
      .when().delete("/interno/boletos/33333333-3333-3333-3333-333333333333")
      .then()
      .statusCode(404)
      .body("codigo", equalTo("NOT_FOUND"));
  }
}
