package br.com.caixa.cobranca.emissor.repositories;

import br.com.caixa.cobranca.emissor.models.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<Usuario, Long> {

  public Optional<Usuario> buscarPorUsername(String username) {
    return find("username", username).firstResultOptional();
  }
}
