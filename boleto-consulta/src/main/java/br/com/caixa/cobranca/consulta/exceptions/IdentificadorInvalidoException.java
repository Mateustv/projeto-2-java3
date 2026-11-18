package br.com.caixa.cobranca.consulta.exceptions;

public class IdentificadorInvalidoException extends RuntimeException {

  public IdentificadorInvalidoException() {
    super("identificador deve ser UUID valido");
  }
}
