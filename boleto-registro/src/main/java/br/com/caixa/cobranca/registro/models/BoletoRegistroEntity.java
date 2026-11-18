package br.com.caixa.cobranca.registro.models;

import br.com.caixa.cobranca.registro.dtos.BoletoConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "boleto_registro")
public class BoletoRegistroEntity {

  @Id
  public UUID identificador;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public StatusBoleto status;

  @Column(name = "data_hora_recebimento", nullable = false)
  public LocalDateTime dataHoraRecebimento;

  @Column(name = "data_hora_registro", nullable = false)
  public LocalDateTime dataHoraRegistro;

  public static BoletoRegistroEntity from(BoletoConsulta dto) {
    BoletoRegistroEntity entity = new BoletoRegistroEntity();
    entity.identificador = dto.identificador();
    entity.status = dto.status();
    entity.dataHoraRecebimento = dto.dataHoraRecebimento();
    entity.dataHoraRegistro = dto.dataHoraRegistro();
    return entity;
  }

  public BoletoConsulta toDto() {
    return new BoletoConsulta(
      identificador,
      status,
      dataHoraRecebimento,
      dataHoraRegistro
    );
  }
}
