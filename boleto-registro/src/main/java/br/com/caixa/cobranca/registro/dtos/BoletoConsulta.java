package br.com.caixa.cobranca.registro.dtos;

import br.com.caixa.cobranca.registro.models.StatusBoleto;
import java.time.LocalDateTime;
import java.util.UUID;

public record BoletoConsulta(
  UUID identificador,
  StatusBoleto status,
  LocalDateTime dataHoraRecebimento,
  LocalDateTime dataHoraRegistro
) {
}
