package br.com.caixa.cobranca.emissor.exceptions;

public class RegistroRejeitadoException extends RuntimeException {

  public RegistroRejeitadoException(int status) {
    super("servico de registro rejeitou o comando com status " + status);
  }
}
