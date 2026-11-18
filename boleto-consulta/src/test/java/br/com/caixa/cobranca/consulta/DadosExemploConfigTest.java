package br.com.caixa.cobranca.consulta;

import static io.restassured.RestAssured.given;

import br.com.caixa.cobranca.consulta.security.SecurityRoles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(DadosExemploConfigTest.SemDadosExemploProfile.class)
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class DadosExemploConfigTest {

  @Test
  void naoCarregaDadosDeExemploQuandoConfiguracaoEstaDesligada() {
    given()
      .when().get("/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(404);
  }

  public static class SemDadosExemploProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("cobranca.consulta.carregar-exemplo", "false");
    }
  }
}
