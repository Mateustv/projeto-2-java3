package br.com.caixa.cobranca.emissor.repositories;

import br.com.caixa.cobranca.emissor.dtos.BoletoAceito;
import br.com.caixa.cobranca.emissor.models.BoletoEmissaoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EmissaoRepository implements PanacheRepositoryBase<BoletoEmissaoEntity, UUID> {

  public Optional<BoletoAceito> buscarPorId(UUID identificador) {
    return findByIdOptional(identificador)
      .map(BoletoEmissaoEntity::toDto);
  }

  public Optional<BoletoAceito> buscarPorChave(String convenio, String numeroControleCliente) {
    return find("convenio = ?1 and numeroControleCliente = ?2", convenio, numeroControleCliente)
      .firstResultOptional()
      .map(BoletoEmissaoEntity::toDto);
  }

  public void salvar(BoletoAceito boleto) {
    persist(BoletoEmissaoEntity.from(boleto));
  }
}
