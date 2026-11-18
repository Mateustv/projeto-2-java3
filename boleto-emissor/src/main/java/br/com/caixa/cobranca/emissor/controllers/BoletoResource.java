package br.com.caixa.cobranca.emissor.controllers;

import br.com.caixa.cobranca.emissor.dtos.BoletoAceito;
import br.com.caixa.cobranca.emissor.dtos.EmitirBoletoRequest;
import br.com.caixa.cobranca.emissor.security.SecurityRoles;
import br.com.caixa.cobranca.emissor.services.EmissaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/boletos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BoletoResource {

  private final EmissaoService service;

  public BoletoResource(EmissaoService service) {
    this.service = service;
  }

  @POST
  @RolesAllowed(SecurityRoles.OPERADOR)
  public RestResponse<BoletoAceito> emitir(
    @Valid @NotNull(message = "corpo da requisicao e obrigatorio") EmitirBoletoRequest request,
    @Context UriInfo uriInfo
  ) {
    BoletoAceito aceite = service.aceitar(request);
    URI location = uriInfo.getAbsolutePathBuilder().path(aceite.identificador().toString()).build();
    return RestResponse.ResponseBuilder.<BoletoAceito>created(location)
      .entity(aceite)
      .build();
  }
}
