package br.com.caixa.cobranca.emissor.services;

import br.com.caixa.cobranca.emissor.dtos.BoletoAceito;
import br.com.caixa.cobranca.emissor.dtos.EmitirBoletoRequest;
import br.com.caixa.cobranca.emissor.exceptions.ConflitoException;
import br.com.caixa.cobranca.emissor.exceptions.RegraNegocioException;
import br.com.caixa.cobranca.emissor.models.StatusBoleto;
import br.com.caixa.cobranca.emissor.repositories.EmissaoRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmissaoService {

  private final EmissaoRepository repository;
  private final RegistroGateway registroGateway;
  private final BigDecimal limiteValorNominal;
  private final Counter solicitacoesEmissao;

  public EmissaoService(
    EmissaoRepository repository,
    RegistroGateway registroGateway,
    @ConfigProperty(name = "cobranca.boleto.valor-maximo") BigDecimal limiteValorNominal,
    MeterRegistry meterRegistry
  ) {
    this.repository = repository;
    this.registroGateway = registroGateway;
    this.limiteValorNominal = limiteValorNominal;
    this.solicitacoesEmissao = meterRegistry.counter("cobranca.boletos.emissao.solicitacoes");
  }

  @Transactional
  public BoletoAceito aceitar(EmitirBoletoRequest request) {
    solicitacoesEmissao.increment();

    repository.buscarPorChave(request.convenio(), request.numeroControleCliente())
      .ifPresent(boleto -> {
        throw new ConflitoException(boleto.identificador());
      });

    if (request.valorNominal().compareTo(limiteValorNominal) > 0) {
      throw new RegraNegocioException("valorNominal acima do limite permitido");
    }

    BoletoAceito aceite = new BoletoAceito(
      UUID.randomUUID(),
      request.convenio(),
      request.numeroControleCliente(),
      request.valorNominal(),
      StatusBoleto.AGUARDANDO_REGISTRO,
      LocalDateTime.now()
    );

    try {
      repository.salvar(aceite);
      repository.flush();
    } catch (PersistenceException ex) {
      throw new ConflitoException(aceite.identificador());
    }

    registroGateway.registrar(aceite.identificador());
    return aceite;
  }

  public java.util.Optional<BoletoAceito> buscarAceite(UUID identificador) {
    return repository.buscarPorId(identificador);
  }
}
