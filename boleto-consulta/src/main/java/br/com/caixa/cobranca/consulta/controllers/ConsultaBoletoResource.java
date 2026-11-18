package br.com.caixa.cobranca.consulta.controllers;

import br.com.caixa.cobranca.consulta.dtos.BoletoConsulta;
import br.com.caixa.cobranca.consulta.exceptions.IdentificadorInvalidoException;
import br.com.caixa.cobranca.consulta.security.SecurityRoles;
import br.com.caixa.cobranca.consulta.services.ConsultaBoletoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/boletos")
@Produces(MediaType.APPLICATION_JSON)
public class ConsultaBoletoResource {

  private final ConsultaBoletoService service;

  public ConsultaBoletoResource(ConsultaBoletoService service) {
    this.service = service;
  }

  @GET
  @Path("/{identificador}")
  @RolesAllowed({ SecurityRoles.OPERADOR, SecurityRoles.AUDITOR })
  public BoletoConsulta consultar(@PathParam("identificador") String identificador) {
    UUID boletoId;
    try {
      boletoId = UUID.fromString(identificador);
    } catch (IllegalArgumentException ex) {
      throw new IdentificadorInvalidoException();
    }
    return service.consultar(boletoId);
  }
}
