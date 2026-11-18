package br.com.caixa.cobranca.consulta.dtos;

import br.com.caixa.cobranca.consulta.models.StatusBoleto;
import java.time.LocalDateTime;
import java.util.UUID;

public record BoletoConsulta(
  UUID identificador,
  StatusBoleto status,
  LocalDateTime dataHoraRecebimento,
  LocalDateTime dataHoraRegistro
) {
}
