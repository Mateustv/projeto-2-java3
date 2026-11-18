package br.com.caixa.cobranca.consulta;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.caixa.cobranca.consulta.models.Usuario;
import br.com.caixa.cobranca.consulta.repositories.UsuarioRepository;
import br.com.caixa.cobranca.consulta.security.SecurityRoles;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AutenticacaoConsultaBoletoResourceTest {

  @Inject
  UsuarioRepository usuarioRepository;

  @Test
  void semCredencialRetorna401ComDesafioBasic() {
    given()
      .when().get("/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(401)
      .header("WWW-Authenticate", containsStringIgnoringCase("Basic"));
  }

  @Test
  void senhaIncorretaRetorna401() {
    given()
      .auth().preemptive().basic("operador", "senha-errada")
      .when().get("/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(401);
  }

  @Test
  void operadorComSenhaCorretaAutenticaEConsulta() {
    given()
      .auth().preemptive().basic("operador", "operador123")
      .when().get("/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(200)
      .body("status", equalTo("AGUARDANDO_REGISTRO"));
  }

  @Test
  void auditorComSenhaCorretaAutenticaEConsulta() {
    given()
      .auth().preemptive().basic("auditor", "auditor123")
      .when().get("/boletos/11111111-1111-1111-1111-111111111111")
      .then()
      .statusCode(200)
      .body("status", equalTo("AGUARDANDO_REGISTRO"));
  }

  @Test
  void papelAutorizadoMantemRetorno404QuandoBoletoNaoExiste() {
    given()
      .auth().preemptive().basic("auditor", "auditor123")
      .when().get("/boletos/22222222-2222-2222-2222-222222222222")
      .then()
      .statusCode(404)
      .body("status", equalTo(404))
      .body("codigo", equalTo("NOT_FOUND"));
  }

  @Test
  void senhaPersistidaEstaHasheadaComBcrypt() {
    Usuario operador = usuarioRepository.buscarPorUsername("operador")
      .orElseThrow(() -> new AssertionError("usuario operador nao encontrado"));

    assertNotEquals("operador123", operador.senha);
    assertTrue(operador.senha.startsWith("$2"));
    assertTrue(BcryptUtil.matches("operador123", operador.senha));
    assertTrue(operador.papeis.contains(SecurityRoles.OPERADOR));
  }
}
