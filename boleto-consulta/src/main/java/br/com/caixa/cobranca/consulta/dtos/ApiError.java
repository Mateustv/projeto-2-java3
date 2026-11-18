package br.com.caixa.cobranca.consulta.dtos;

public record ApiError(int status, String codigo, String mensagem) {
}
