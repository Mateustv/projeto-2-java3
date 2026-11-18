package br.com.caixa.cobranca.emissor.controllers;

import br.com.caixa.cobranca.emissor.dtos.BoletoAceito;
import br.com.caixa.cobranca.emissor.exceptions.RecursoNaoEncontradoException;
import br.com.caixa.cobranca.emissor.services.EmissaoService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/interno/boletos")
@Produces(MediaType.APPLICATION_JSON)
public class ConsultaInternaBoletoResource {

  private final EmissaoService service;

  public ConsultaInternaBoletoResource(EmissaoService service) {
    this.service = service;
  }

  @GET
  @Path("/{identificador}")
  public BoletoAceito consultar(@PathParam("identificador") UUID identificador) {
    return service.buscarAceite(identificador)
      .orElseThrow(() -> new RecursoNaoEncontradoException("boleto nao encontrado"));
  }
}
