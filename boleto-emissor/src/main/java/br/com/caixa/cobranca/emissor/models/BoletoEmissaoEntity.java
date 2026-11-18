package br.com.caixa.cobranca.emissor.models;

import br.com.caixa.cobranca.emissor.dtos.BoletoAceito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
  name = "boleto_emissao",
  uniqueConstraints = @UniqueConstraint(
    name = "uk_emissao_convenio_controle",
    columnNames = {"convenio", "numero_controle_cliente"}
  )
)
public class BoletoEmissaoEntity {

  @Id
  public UUID identificador;

  @Column(nullable = false)
  public String convenio;

  @Column(name = "numero_controle_cliente", nullable = false)
  public String numeroControleCliente;

  @Column(name = "valor_nominal", nullable = false, precision = 15, scale = 2)
  public BigDecimal valorNominal;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public StatusBoleto status;

  @Column(name = "data_hora_recebimento", nullable = false)
  public LocalDateTime dataHoraRecebimento;

  public static BoletoEmissaoEntity from(BoletoAceito dto) {
    BoletoEmissaoEntity entity = new BoletoEmissaoEntity();
    entity.identificador = dto.identificador();
    entity.convenio = dto.convenio();
    entity.numeroControleCliente = dto.numeroControleCliente();
    entity.valorNominal = dto.valorNominal();
    entity.status = dto.status();
    entity.dataHoraRecebimento = dto.dataHoraRecebimento();
    return entity;
  }

  public BoletoAceito toDto() {
    return new BoletoAceito(
      identificador,
      convenio,
      numeroControleCliente,
      valorNominal,
      status,
      dataHoraRecebimento
    );
  }
}
