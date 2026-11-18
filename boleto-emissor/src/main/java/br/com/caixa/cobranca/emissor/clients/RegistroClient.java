package br.com.caixa.cobranca.emissor.clients;

import br.com.caixa.cobranca.emissor.dtos.RegistrarBoletoCommand;
import br.com.caixa.cobranca.emissor.exceptions.RegistroRejeitadoException;
import io.quarkus.rest.client.reactive.ClientBasicAuth;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/interno/comandos/registro")
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "registro")
@ClientBasicAuth(username = "${registro.auth.username}", password = "${registro.auth.password}")
public interface RegistroClient {

  @POST
  void registrar(RegistrarBoletoCommand comando);

  @ClientExceptionMapper
  static RuntimeException mapearErro(Response response) {
    int status = response.getStatus();
    return status >= 400 && status < 500
      ? new RegistroRejeitadoException(status)
      : null;
  }
}
