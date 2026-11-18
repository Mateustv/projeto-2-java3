package br.com.caixa.cobranca.emissor.exceptions;

public class RegistroIndisponivelException extends RuntimeException {

  public RegistroIndisponivelException() {
    super("servico de registro indisponivel; tente novamente em instantes");
  }
}
