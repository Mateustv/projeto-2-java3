package br.com.caixa.cobranca.registro.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegistrarBoletoCommand(
  @NotNull(message = "identificador e obrigatorio") UUID identificador
) {
}
