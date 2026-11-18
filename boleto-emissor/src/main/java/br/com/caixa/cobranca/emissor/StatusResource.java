package br.com.caixa.cobranca.emissor;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/status")
public class StatusResource {

  @GET
  public StatusResponse status() {
    return new StatusResponse("boleto-emissor", "UP");
  }
}
