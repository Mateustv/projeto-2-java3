package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.caixa.cobranca.emissor.clients.RegistroClient;
import br.com.caixa.cobranca.emissor.models.Usuario;
import br.com.caixa.cobranca.emissor.repositories.UsuarioRepository;
import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AutenticacaoBoletoResourceTest {

  @InjectMock
  @RestClient
  RegistroClient registro;

  @jakarta.inject.Inject
  UsuarioRepository usuarioRepository;

  @Test
  void deveRetornar401SemCredencialEComDesafioBasic() {
    given()
      .contentType(ContentType.JSON)
      .body(payloadValido("SEM-CREDENCIAL-" + UUID.randomUUID()))
      .when().post("/boletos")
      .then()
      .statusCode(401)
        .header("WWW-Authenticate", containsStringIgnoringCase("Basic"));
  }

  @Test
  void deveRetornar401QuandoSenhaIncorreta() {
    given()
      .auth().preemptive().basic("operador", "senha-errada")
      .contentType(ContentType.JSON)
      .body(payloadValido("SENHA-INVALIDA-" + UUID.randomUUID()))
      .when().post("/boletos")
      .then()
      .statusCode(401);
  }

  @Test
  void auditorAutenticadoSemPermissaoDeveRetornar403ComCorpoPadronizado() {
    Response resposta = given()
      .auth().preemptive().basic("auditor", "auditor123")
      .contentType(ContentType.JSON)
      .body(payloadValido("AUDITOR-SEM-PERMISSAO-" + UUID.randomUUID()))
      .when().post("/boletos");

    assertEquals(403, resposta.statusCode());
    String contentType = resposta.getContentType();
    if (contentType != null && contentType.contains("application/json")) {
      resposta.then()
        .body("status", equalTo(403))
        .body("codigo", equalTo("FORBIDDEN"))
        .body("mensagem", equalTo("acesso negado"));
    }
  }

  @Test
  void operadorComSenhaCorretaPassaPelaAutenticacao() {
    given()
      .auth().preemptive().basic("operador", "operador123")
      .contentType(ContentType.JSON)
      .body(payloadValido("OPERADOR-OK-" + UUID.randomUUID()))
      .when().post("/boletos")
      .then()
      .statusCode(201)
      .body("status", equalTo("AGUARDANDO_REGISTRO"));
  }

  @Test
  void senhaPersistidaDoOperadorEstaHasheadaComBcrypt() {
    Usuario operador = usuarioRepository.buscarPorUsername("operador")
      .orElseThrow(() -> new AssertionError("usuario operador nao encontrado"));

    assertNotEquals("operador123", operador.senha);
    assertTrue(operador.senha.startsWith("$2"));
    assertTrue(BcryptUtil.matches("operador123", operador.senha));
    assertTrue(operador.papeis.contains(SecurityRoles.OPERADOR));
  }

  private static String payloadValido(String numeroControleCliente) {
    return """
      {
        "convenio": "123456",
        "numeroControleCliente": "%s",
        "valorNominal": 50.00
      }
      """.formatted(numeroControleCliente);
  }
}
