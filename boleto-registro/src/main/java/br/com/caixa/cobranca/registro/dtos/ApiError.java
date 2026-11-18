package br.com.caixa.cobranca.registro.dtos;

public record ApiError(int status, String codigo, String mensagem) {
}
