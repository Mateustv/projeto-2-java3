package br.com.caixa.cobranca.emissor.services;

import br.com.caixa.cobranca.emissor.clients.RegistroClient;
import br.com.caixa.cobranca.emissor.dtos.RegistrarBoletoCommand;
import br.com.caixa.cobranca.emissor.exceptions.RegistroIndisponivelException;
import br.com.caixa.cobranca.emissor.exceptions.RegistroRejeitadoException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class RegistroGateway {

  private final RegistroClient client;

  public RegistroGateway(@RestClient RegistroClient client) {
    this.client = client;
  }

  @Timeout(500)
  @Retry(maxRetries = 2, abortOn = RegistroRejeitadoException.class)
  @CircuitBreaker(requestVolumeThreshold = 4, skipOn = RegistroRejeitadoException.class)
  @Fallback(fallbackMethod = "registroIndisponivel", skipOn = RegistroRejeitadoException.class)
  public void  registrar(UUID identificador) {
    client.registrar(new RegistrarBoletoCommand(identificador));
  }

  void registroIndisponivel(UUID identificador) {
    throw new RegistroIndisponivelException();
  }
}

