package br.com.caixa.cobranca.consulta.repositories;

import br.com.caixa.cobranca.consulta.dtos.BoletoConsulta;
import br.com.caixa.cobranca.consulta.models.BoletoConsultaEntity;
import br.com.caixa.cobranca.consulta.models.StatusBoleto;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConsultaRepository implements PanacheRepositoryBase<BoletoConsultaEntity, UUID> {

  private final boolean carregarExemplo;

  public ConsultaRepository(
    @ConfigProperty(name = "cobranca.consulta.carregar-exemplo") boolean carregarExemplo
  ) {
    this.carregarExemplo = carregarExemplo;
  }

  @Transactional
  void init(@Observes StartupEvent event) {
    if (!carregarExemplo) {
      return;
    }

    UUID identificador = UUID.fromString("11111111-1111-1111-1111-111111111111");

    if (findByIdOptional(identificador).isPresent()) {
      return;
    }

    BoletoConsulta boleto = new BoletoConsulta(
      identificador,
      StatusBoleto.AGUARDANDO_REGISTRO,
      LocalDateTime.now(),
      null
    );
    persist(BoletoConsultaEntity.from(boleto));
  }

  public Optional<BoletoConsulta> buscar(UUID identificador) {
    return findByIdOptional(identificador)
      .map(BoletoConsultaEntity::toDto);
  }
}
