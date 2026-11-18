package br.com.caixa.cobranca.emissor.dtos;

import br.com.caixa.cobranca.emissor.models.StatusBoleto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BoletoAceito(
  UUID identificador,
  String convenio,
  String numeroControleCliente,
  BigDecimal valorNominal,
  StatusBoleto status,
  LocalDateTime dataHoraRecebimento
) {
}
