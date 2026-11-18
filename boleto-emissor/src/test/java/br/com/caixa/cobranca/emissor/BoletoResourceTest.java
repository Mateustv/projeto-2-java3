package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import br.com.caixa.cobranca.emissor.clients.RegistroClient;
import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class BoletoResourceTest {

  @InjectMock
  @RestClient
  RegistroClient registro;

  @Test
  void deveCriarBoletoCom201() {
    String sufixo = String.valueOf(System.nanoTime());

    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "convenio": "123456",
          "numeroControleCliente": "CTRL-%s",
          "valorNominal": 500.00
        }
        """.formatted(sufixo))
      .when().post("/boletos")
      .then()
      .statusCode(201)
      .header("Location", notNullValue())
      .body("status", equalTo("AGUARDANDO_REGISTRO"));
  }

  @Test
  void deveRetornar409QuandoDuplicado() {
    String body = """
      {
        "convenio": "654321",
        "numeroControleCliente": "CTRL-DUPLICADO",
        "valorNominal": 300.00
      }
      """;

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when().post("/boletos")
      .then()
      .statusCode(201);

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when().post("/boletos")
      .then()
      .statusCode(409)
      .body("status", equalTo(409))
      .body("codigo", equalTo("CONFLICT"))
      .body("mensagem", containsString("boleto ja aceito"));
  }

  @Test
  void deveRetornar422ParaRegraDeNegocio() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "convenio": "123456",
          "numeroControleCliente": "CTRL-LIMITE",
          "valorNominal": 1000000.00
        }
        """)
      .when().post("/boletos")
      .then()
      .statusCode(422)
      .body("status", equalTo(422))
      .body("codigo", equalTo("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void deveRetornar400ParaPayloadInvalido() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "numeroControleCliente": "CTRL-SEM-CONVENIO",
          "valorNominal": 50.00
        }
        """)
      .when().post("/boletos")
      .then()
      .statusCode(400)
      .body("status", equalTo(400))
      .body("codigo", equalTo("BAD_REQUEST"))
      .body("mensagem", containsString("convenio e obrigatorio"));
  }

  @Test
  void deveRetornar404NaConsultaInternaQuandoNaoEncontrar() {
    given()
      .when().get("/interno/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(404)
      .body("status", equalTo(404))
      .body("codigo", equalTo("NOT_FOUND"));
  }

  @Test
  void errosDeRotaMantemStatusEEnvelopePadrao() {
    given()
      .when().get("/rota-inexistente")
      .then()
      .statusCode(404)
      .contentType(ContentType.JSON)
      .body("status", equalTo(404))
      .body("codigo", equalTo("NOT_FOUND"));

    given()
      .when().patch("/boletos")
      .then()
      .statusCode(405)
      .contentType(ContentType.JSON)
      .body("status", equalTo(405))
      .body("codigo", equalTo("METHOD_NOT_ALLOWED"));
  }

  @Test
  void deveConsultarInternoCom200AposCriacao() {
    String sufixo = String.valueOf(System.nanoTime());

    Response respostaCriacao = given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "convenio": "123456",
          "numeroControleCliente": "CTRL-GET-%s",
          "valorNominal": 150.00
        }
        """.formatted(sufixo))
      .when().post("/boletos");

    String id = respostaCriacao.jsonPath().getString("identificador");

    given()
      .when().get("/interno/boletos/" + id)
      .then()
      .statusCode(200)
      .body("identificador", equalTo(id));
  }
}
