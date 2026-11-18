package br.com.caixa.cobranca.registro.services;

import br.com.caixa.cobranca.registro.dtos.BoletoConsulta;
import br.com.caixa.cobranca.registro.dtos.RegistrarBoletoCommand;
import br.com.caixa.cobranca.registro.models.StatusBoleto;
import br.com.caixa.cobranca.registro.repositories.RegistroRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class RegistroBoletoService {

  private final RegistroRepository repository;

  public RegistroBoletoService(RegistroRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public ResultadoRegistro registrar(RegistrarBoletoCommand comando) {
    Optional<BoletoConsulta> existente = repository.buscar(comando.identificador());
    if (existente.isPresent()) {
      return new ResultadoRegistro(existente.get(), true);
    }

    LocalDateTime agora = LocalDateTime.now();
    BoletoConsulta novo = new BoletoConsulta(
      comando.identificador(),
      StatusBoleto.REGISTRADO,
      agora,
      agora
    );
    repository.salvar(novo);
    return new ResultadoRegistro(novo, false);
  }

  public Optional<BoletoConsulta> buscar(java.util.UUID identificador) {
    return repository.buscar(identificador);
  }

  @Transactional
  public boolean remover(java.util.UUID identificador) {
    return repository.remover(identificador);
  }

  public record ResultadoRegistro(BoletoConsulta boleto, boolean duplicado) {
  }
}
