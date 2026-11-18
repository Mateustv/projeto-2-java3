package br.com.caixa.cobranca.emissor.exceptions;

import java.util.UUID;

public class ConflitoException extends RuntimeException {

  public ConflitoException(UUID identificador) {
    super("boleto ja aceito para a chave de idempotencia. identificador=" + identificador);
  }
}
