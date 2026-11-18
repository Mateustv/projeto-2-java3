package br.com.caixa.cobranca.emissor.dtos;

public record ApiError(int status, String codigo, String mensagem) {
}
