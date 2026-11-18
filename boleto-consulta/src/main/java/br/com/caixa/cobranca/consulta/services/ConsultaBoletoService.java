package br.com.caixa.cobranca.consulta.services;

import br.com.caixa.cobranca.consulta.dtos.BoletoConsulta;
import br.com.caixa.cobranca.consulta.exceptions.RecursoNaoEncontradoException;
import br.com.caixa.cobranca.consulta.repositories.ConsultaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ConsultaBoletoService {

  private final ConsultaRepository repository;

  public ConsultaBoletoService(ConsultaRepository repository) {
    this.repository = repository;
  }

  public BoletoConsulta consultar(UUID identificador) {
    return repository.buscar(identificador)
      .orElseThrow(() -> new RecursoNaoEncontradoException("boleto nao encontrado"));
  }
}
