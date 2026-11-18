package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import br.com.caixa.cobranca.emissor.clients.RegistroClient;
import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class ObservabilidadeTest {

  @InjectMock
  @RestClient
  RegistroClient registro;

  @Test
  void healthLiveEResponseReadyFicamUp() {
    given()
      .when().get("/q/health/live")
      .then()
      .statusCode(200)
      .body("status", equalTo("UP"));

    given()
      .when().get("/q/health/ready")
      .then()
      .statusCode(200)
      .body("status", equalTo("UP"));
  }

  @Test
  void metricaDeSolicitacoesDeEmissaoEstaExposta() {
    String controle = "CTRL-METRICA-" + UUID.randomUUID();

    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "convenio": "123456",
          "numeroControleCliente": "%s",
          "valorNominal": 50.00
        }
        """.formatted(controle))
      .when().post("/boletos")
      .then()
      .statusCode(201);

    given()
      .when().get("/q/metrics")
      .then()
      .statusCode(200)
      .body(containsString("cobranca_boletos_emissao_solicitacoes_total"));
  }
}
