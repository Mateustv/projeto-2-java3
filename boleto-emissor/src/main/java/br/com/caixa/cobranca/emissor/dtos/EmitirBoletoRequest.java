package br.com.caixa.cobranca.emissor.dtos;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record EmitirBoletoRequest(
  @NotBlank(message = "convenio e obrigatorio") String convenio,
  @NotBlank(message = "numeroControleCliente e obrigatorio") String numeroControleCliente,
  @NotNull(message = "valorNominal e obrigatorio")
  @Positive(message = "valorNominal deve ser maior que zero")
  @Digits(integer = 12, fraction = 2, message = "valorNominal deve ter ate 12 digitos inteiros e 2 decimais")
  BigDecimal valorNominal
) {
}
