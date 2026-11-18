package br.com.caixa.cobranca.registro.controllers;

import br.com.caixa.cobranca.registro.dtos.BoletoConsulta;
import br.com.caixa.cobranca.registro.dtos.RegistrarBoletoCommand;
import br.com.caixa.cobranca.registro.security.SecurityRoles;
import br.com.caixa.cobranca.registro.services.RegistroBoletoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/interno/comandos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegistroComandoResource {

  private final RegistroBoletoService service;

  public RegistroComandoResource(RegistroBoletoService service) {
    this.service = service;
  }

  @POST
  @Path("/registro")
  @RolesAllowed(SecurityRoles.OPERADOR)
  public RestResponse<BoletoConsulta> registrar(
    @Valid @NotNull(message = "corpo da requisicao e obrigatorio") RegistrarBoletoCommand comando
  ) {
    RegistroBoletoService.ResultadoRegistro resultado = service.registrar(comando);
    if (resultado.duplicado()) {
      return RestResponse.ok(resultado.boleto());
    }
    return RestResponse.ResponseBuilder.<BoletoConsulta>create(RestResponse.StatusCode.CREATED)
      .entity(resultado.boleto())
      .build();
  }
}
