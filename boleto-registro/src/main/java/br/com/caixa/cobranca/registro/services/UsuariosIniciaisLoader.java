package br.com.caixa.cobranca.registro.services;

import br.com.caixa.cobranca.registro.models.Usuario;
import br.com.caixa.cobranca.registro.repositories.UsuarioRepository;
import br.com.caixa.cobranca.registro.security.SecurityRoles;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UsuariosIniciaisLoader {

  private final UsuarioRepository usuarioRepository;
  private final boolean seedUsers;

  public UsuariosIniciaisLoader(
    UsuarioRepository usuarioRepository,
    @ConfigProperty(name = "cobranca.security.seed-users") boolean seedUsers
  ) {
    this.usuarioRepository = usuarioRepository;
    this.seedUsers = seedUsers;
  }

  @Transactional
  void init(@Observes StartupEvent event) {
    if (!seedUsers) {
      return;
    }

    cadastrarSeAusente("operador", "operador123", SecurityRoles.OPERADOR);
    cadastrarSeAusente("auditor", "auditor123", SecurityRoles.AUDITOR);
  }

  private void cadastrarSeAusente(String username, String senhaEmTextoPlano, String papeis) {
    if (usuarioRepository.buscarPorUsername(username).isPresent()) {
      return;
    }

    Usuario usuario = new Usuario();
    usuario.username = username;
    usuario.senha = BcryptUtil.bcryptHash(senhaEmTextoPlano);
    usuario.papeis = papeis;
    usuarioRepository.persist(usuario);
  }
}
