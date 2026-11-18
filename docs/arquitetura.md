# Arquitetura - Estado Final (Etapas 1 a 9)

## Servicos, portas e dados

| Servico | Porta | Banco | Responsabilidade |
|---|---:|---|---|
| `boleto-emissor` | 8080 | H2 proprio | Recebe emissao de boletos e inicia o fluxo de registro. |
| `boleto-registro` | 8081 | H2 proprio | Registra identificadores de boletos com idempotencia por UUID. |
| `boleto-consulta` | 8082 | H2 proprio | Disponibiliza consulta de boletos para consumidores externos. |

Emissor e Registro ocupam as portas `8080` e `8081`, conforme o brief. Consulta é um terceiro serviço adicional na porta `8082`.
Cada modulo Quarkus e independente e utiliza datasource H2 separado por perfil.

## Endpoints por fronteira

Externos:

- Emissor: `POST /boletos`
- Consulta: `GET /boletos/{identificador}`

Internos (`/interno/*`):

- Emissor: `GET /interno/boletos/{identificador}`
- Registro: `POST /interno/comandos/registro`
- Registro: `GET /interno/boletos/{identificador}`
- Registro: `DELETE /interno/boletos/{identificador}`

Operacionais:

- `/status`
- `/q/health/live`
- `/q/health/ready`
- `/q/metrics`
- `/q/openapi`

## Fluxo principal (REST Client)

1. Cliente externo chama `POST /boletos` no emissor.
2. Emissor valida, persiste emissao e chama o registro via REST Client em `POST /interno/comandos/registro`.
3. Registro grava o UUID do boleto.
4. Em caso de repeticao do mesmo UUID, registro retorna o recurso ja existente (idempotencia).

## Fault Tolerance no emissor

Configurado no gateway de integracao com o registro:

- Timeout: 500 ms
- Retry: maxRetries=2
- Circuit Breaker: requestVolumeThreshold=4
- Fallback: indisponibilidade mapeada para erro controlado

Comportamento esperado:

- Falha transiente: retry automatico.
- Falha recorrente: circuito abre e evita novas chamadas remotas por um periodo.
- Indisponibilidade/timeout/circuito aberto: emissor retorna `503`.
- 4xx inesperado do registro: emissor retorna `502` sem retry.

A estrategia de retry e segura porque o comando no registro e idempotente pelo identificador UUID.

## Seguranca

Autenticacao:

- Basic Auth com usuarios em banco via `quarkus-security-jpa`.
- Senhas armazenadas com BCrypt.
- Usuarios ficticios de dev/test: `operador` e `auditor`.

RBAC humano:

- `POST /boletos`: apenas `operador`.
- `GET /boletos/{identificador}`: `operador` e `auditor`.
- `POST /interno/comandos/registro`: apenas `operador`.
- `GET /interno/boletos/{identificador}`: `operador` e `auditor`.
- `DELETE /interno/boletos/{identificador}`: apenas `operador`.

Integracao tecnica Emissor -> Registro:

- O Emissor autentica no Registro via Basic Auth no REST Client com credenciais definidas por configuracao de ambiente.

## Observabilidade

Todos os modulos expõem:

- `/q/health/live`
- `/q/health/ready`
- `/q/metrics`

Metrica de negocio adicional:

- `cobranca_boletos_emissao_solicitacoes_total` no emissor.
