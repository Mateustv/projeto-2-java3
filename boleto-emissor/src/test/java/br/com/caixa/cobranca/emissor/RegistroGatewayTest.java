package br.com.caixa.cobranca.emissor;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import br.com.caixa.cobranca.emissor.clients.RegistroClient;
import br.com.caixa.cobranca.emissor.dtos.RegistrarBoletoCommand;
import br.com.caixa.cobranca.emissor.exceptions.RegistroRejeitadoException;
import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@TestSecurity(user = "operador", roles = SecurityRoles.OPERADOR)
class RegistroGatewayTest {

  @InjectMock
  @RestClient
  RegistroClient registro;

  @Inject
  CircuitBreakerMaintenance circuitos;

  @BeforeEach
  @AfterEach
  void resetarCircuitos() {
    circuitos.resetAll();
  }

  @Test
  void emissaoRegistraNoServicoDeRegistro() {
    Response resposta = emitir(UUID.randomUUID().toString());
    UUID identificador = UUID.fromString(resposta.jsonPath().getString("identificador"));

    assertEquals(201, resposta.statusCode());
    verify(registro).registrar(argThat(comando -> comando.identificador().equals(identificador)));

    given()
      .when().get("/interno/boletos/" + identificador)
      .then()
      .statusCode(200);
  }

  @Test
  void servicoIndisponivelRetentaEDevolve503SemPersistirEmissao() {
    doThrow(new ProcessingException("Connection refused"))
      .when(registro).registrar(any(RegistrarBoletoCommand.class));

    Response resposta = emitir(UUID.randomUUID().toString());

    assertEquals(503, resposta.statusCode());
    assertEquals(503, resposta.jsonPath().getInt("status"));
    assertEquals("SERVICE_UNAVAILABLE", resposta.jsonPath().getString("codigo"));

    ArgumentCaptor<RegistrarBoletoCommand> comandos =
      ArgumentCaptor.forClass(RegistrarBoletoCommand.class);
    verify(registro, times(3)).registrar(comandos.capture());
    assertEquals(
      1,
      comandos.getAllValues().stream()
        .map(RegistrarBoletoCommand::identificador)
        .distinct()
        .count()
    );

    UUID identificador = comandos.getValue().identificador();
    given()
      .when().get("/interno/boletos/" + identificador)
      .then()
      .statusCode(404);
  }

  @Test
  void timeoutDaChamadaDevolve503() {
    doAnswer(invocacao -> {
      Thread.sleep(800);
      return null;
    }).when(registro).registrar(any(RegistrarBoletoCommand.class));

    assertEquals(503, emitir(UUID.randomUUID().toString()).statusCode());
    verify(registro, atLeastOnce()).registrar(any(RegistrarBoletoCommand.class));
  }

  @Test
  void resposta4xxDoRegistroNaoERepetidaNemTratadaComoIndisponibilidade() {
    doThrow(new RegistroRejeitadoException(422))
      .when(registro).registrar(any(RegistrarBoletoCommand.class));

    Response resposta = emitir(UUID.randomUUID().toString());
    assertEquals(502, resposta.statusCode());
    assertEquals(502, resposta.jsonPath().getInt("status"));
    assertEquals("BAD_GATEWAY", resposta.jsonPath().getString("codigo"));
    verify(registro, times(1)).registrar(any(RegistrarBoletoCommand.class));
  }

  @Test
  void circuitoAbertoFalhaSemChamarServicoRemoto() {
    doThrow(new ProcessingException("Connection refused"))
      .when(registro).registrar(any(RegistrarBoletoCommand.class));

    assertEquals(503, emitir(UUID.randomUUID().toString()).statusCode());
    assertEquals(503, emitir(UUID.randomUUID().toString()).statusCode());
    verify(registro, times(4)).registrar(any(RegistrarBoletoCommand.class));
    clearInvocations(registro);

    assertEquals(503, emitir(UUID.randomUUID().toString()).statusCode());
    verifyNoInteractions(registro);
  }

  private static Response emitir(String numeroControle) {
    return given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "convenio": "TESTE",
          "numeroControleCliente": "%s",
          "valorNominal": 50.00
        }
        """.formatted(numeroControle))
      .when().post("/boletos");
  }
}
