package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;

import br.com.caixa.cobranca.emissor.clients.RegistroClient;
import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(LimiteEmissaoConfigTest.LimiteBaixoProfile.class)
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class LimiteEmissaoConfigTest {

  @InjectMock
  @RestClient
  RegistroClient registro;

  @Test
  void respeitaOLimiteMaximoConfigurado() {
    emitir("10.00").statusCode(201);
    emitir("10.01").statusCode(422);
  }

  private static io.restassured.response.ValidatableResponse emitir(String valor) {
    return given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "convenio": "TESTE",
          "numeroControleCliente": "%s",
          "valorNominal": %s
        }
        """.formatted(UUID.randomUUID(), valor))
      .when().post("/boletos")
      .then();
  }

  public static class LimiteBaixoProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("cobranca.boleto.valor-maximo", "10.00");
    }
  }
}
