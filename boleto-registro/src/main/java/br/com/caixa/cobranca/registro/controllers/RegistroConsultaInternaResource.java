package br.com.caixa.cobranca.registro.controllers;

import br.com.caixa.cobranca.registro.dtos.BoletoConsulta;
import br.com.caixa.cobranca.registro.exceptions.RecursoNaoEncontradoException;
import br.com.caixa.cobranca.registro.security.SecurityRoles;
import br.com.caixa.cobranca.registro.services.RegistroBoletoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/interno/boletos")
@Produces(MediaType.APPLICATION_JSON)
public class RegistroConsultaInternaResource {

  private final RegistroBoletoService service;

  public RegistroConsultaInternaResource(RegistroBoletoService service) {
    this.service = service;
  }

  @GET
  @Path("/{identificador}")
  @RolesAllowed({ SecurityRoles.OPERADOR, SecurityRoles.AUDITOR })
  public BoletoConsulta consultar(@PathParam("identificador") java.util.UUID identificador) {
    return service.buscar(identificador)
      .orElseThrow(() -> new RecursoNaoEncontradoException("boleto nao encontrado"));
  }

  @DELETE
  @Path("/{identificador}")
  @RolesAllowed(SecurityRoles.OPERADOR)
  public RestResponse<Void> remover(@PathParam("identificador") java.util.UUID identificador) {
    if (!service.remover(identificador)) {
      throw new RecursoNaoEncontradoException("boleto nao encontrado");
    }
    return RestResponse.noContent();
  }
}
