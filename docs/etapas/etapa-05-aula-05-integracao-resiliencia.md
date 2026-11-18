# Etapa 05 - Aula 05 - Integracao Sincrona e Resiliencia

## Status
Concluida.

## Objetivo
Implementar comunicacao sincrona entre servicos com tolerancia a falhas completa.

## Escopo
- Incluir: Rest Client, timeout, retry, circuit breaker e fallback.
- Excluir: topicos de mensageria assicrona fora do escopo base.

## Tarefas tecnicas
- [x] Implementar fluxo principal emissor -> registro com REST Client.
- [x] Parametrizar URL, timeout, retentativas e janela do circuit breaker.
- [x] Configurar fallback para indisponibilidade, timeout e circuito aberto.
- [x] Cobrir caminho feliz, indisponibilidade, timeout, circuito aberto e resposta 4xx.
- [x] Verificar que falha remota nao persiste a emissao local e que as retentativas reutilizam o mesmo UUID.

## Criterios de saida
- Integracao executa em caminho feliz.
- Falhas sao tratadas com politica configurada e comportamento testado.

## Criterios do brief cobertos
- C2 (integracao e FT), C7.

## Evidencias esperadas
- cliente REST configurado.
- anotacoes/politicas de FT aplicadas.
- testes de timeout/retry/circuit breaker/fallback.

## Evidencias implementadas
- `boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/clients/RegistroClient.java`: POST para `/interno/comandos/registro`, URL por `quarkus.rest-client.registro.url`; respostas 4xx mapeadas como rejeicao permanente.
- `boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/RegistroGateway.java`: `@Timeout(500)`, `@Retry(maxRetries=2)`, `@CircuitBreaker(requestVolumeThreshold=4)` e `@Fallback`.
- O comando contem o UUID gerado pelo emissor. O registro e idempotente para esse identificador, tornando a repeticao segura.
- Falha tecnica, timeout ou circuito aberto resultam em `503`; resposta 4xx inesperada do servico remoto resulta em `502` sem retry.
- `RegistroGatewayTest` injeta um mock do REST Client e cobre caminho feliz, tres tentativas, timeout, circuito aberto, erro 4xx e ausencia de persistencia local na falha.
- O fluxo remoto ocorre dentro da transacao do emissor: a falha no registro causa rollback da emissao local.
- Validacao: `mvn -B verify` — `BUILD SUCCESS`, 36 testes, 0 falhas e 0 erros.
- Validacao ponta a ponta adicional: os jars de emissor e registro foram iniciados em portas aleatorias e o boleto criado no emissor foi consultado no registro com status `REGISTRADO`.
