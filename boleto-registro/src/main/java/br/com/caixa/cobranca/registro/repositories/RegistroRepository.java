package br.com.caixa.cobranca.registro.repositories;

import br.com.caixa.cobranca.registro.dtos.BoletoConsulta;
import br.com.caixa.cobranca.registro.models.BoletoRegistroEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RegistroRepository implements PanacheRepositoryBase<BoletoRegistroEntity, UUID> {

  public Optional<BoletoConsulta> buscar(UUID identificador) {
    return findByIdOptional(identificador)
      .map(BoletoRegistroEntity::toDto);
  }

  public void salvar(BoletoConsulta boleto) {
    persist(BoletoRegistroEntity.from(boleto));
  }

  public boolean remover(UUID identificador) {
    return deleteById(identificador);
  }
}
