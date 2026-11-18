# Projeto Final em Grupo - Central de Cobranca

Projeto Quarkus multi-modulo com foco em emissao, registro e consulta de boletos, cobrindo os criterios C1 a C9 do brief da disciplina.

## Tema e problema

Tema: cobranca de boletos em fluxo sincrono entre servicos.

Problema: receber pedidos de emissao, registrar o identificador de forma resiliente e disponibilizar consulta do estado, mantendo contrato REST claro, validacao padronizada, persistencia, observabilidade e seguranca com autenticacao e autorizacao por papel.

## Versoes

- Quarkus 3.15.7
- Java 21

## Arquitetura dos servicos

- boleto-emissor (porta 8080): recebe `POST /boletos`, persiste a emissao e chama o registro.
- boleto-registro (porta 8081): recebe `POST /interno/comandos/registro` e mantem registro idempotente por UUID.
- boleto-consulta (porta 8082): expõe `GET /boletos/{identificador}` para leitura.

As portas `8080` e `8081` atendem aos dois serviços exigidos pelo brief; a Consulta é um terceiro serviço adicional na porta `8082`.
Cada modulo possui banco H2 em memoria separado por perfil.

Documentacao de arquitetura: [docs/arquitetura.md](docs/arquitetura.md)

Roteiro para apresentar o codigo e demonstrar os fluxos no Postman: [docs/roteiro-banca-codigo.md](docs/roteiro-banca-codigo.md)

## Endpoints principais

Externos:

- `POST /boletos` (emissor)
- `GET /boletos/{identificador}` (consulta)

Internos:

- `GET /interno/boletos/{identificador}` (emissor)
- `POST /interno/comandos/registro` (registro)
- `GET /interno/boletos/{identificador}` (registro)
- `DELETE /interno/boletos/{identificador}` (registro)

Operacionais:

- `/status`
- `/q/health/live`
- `/q/health/ready`
- `/q/metrics`
- `/q/openapi`
- `/q/swagger-ui` (interface interativa em dev)

## Fluxo Emissor -> Registro (REST Client)

Ao aceitar uma emissao, o `boleto-emissor` chama `POST /interno/comandos/registro` no `boleto-registro` via REST Client.

Resiliencia aplicada no gateway de integracao:

- Timeout de 500 ms
- Retry com 2 retentativas (ate 3 tentativas no total)
- Circuit Breaker com `requestVolumeThreshold=4`
- Fallback para indisponibilidade

Justificativa:

- Timeout evita prender a transacao de emissao por demora externa.
- Retry melhora disponibilidade quando a falha e transiente.
- Circuit Breaker reduz pressao no servico dependente quando ha falhas repetidas.
- Fallback traduz indisponibilidade em resposta de negocio previsivel ao consumidor.

Idempotencia do Retry:

- O retry e seguro porque o registro e idempotente pelo `identificador` UUID.
- Repeticao do mesmo UUID no registro devolve o mesmo recurso (201 na primeira vez, 200 em duplicidade).

Comportamento para indisponibilidade do registro:

- Em falha de integracao (timeout, indisponibilidade ou circuito aberto), o emissor retorna `503 SERVICE_UNAVAILABLE`.
- A emissao local nao permanece persistida quando o registro nao e concluido.
- Para resposta 4xx inesperada do registro, o emissor retorna `502 BAD_GATEWAY` sem retry.

## Seguranca (usuarios, papeis e RBAC)

Usuarios ficticios seeded em dev/test:

- `operador / operador123`
- `auditor / auditor123`

As senhas sao persistidas em hash BCrypt com `quarkus-security-jpa`.

Matriz RBAC vigente:

- operador: pode emitir e consultar
- auditor: pode apenas consultar

Aplicacao:

- `POST /boletos` -> somente operador
- `GET /boletos/{identificador}` -> operador e auditor
- `POST /interno/comandos/registro` -> somente operador
- `GET /interno/boletos/{identificador}` -> operador e auditor
- `DELETE /interno/boletos/{identificador}` -> somente operador

Integracao Emissor -> Registro:

- O Emissor envia Basic Auth para o Registro no REST Client usando credenciais por configuracao (`registro.auth.username` e `registro.auth.password`), sem hardcode no codigo.

## Configuracao por ambiente

Perfis `%dev`, `%test` e `%prod` estao configurados nos modulos com H2 separado e parametros de negocio.

Exemplo de override por variavel de ambiente (PowerShell) para limite de emissao:

```powershell
$env:COBRANCA_BOLETO_VALOR_MAXIMO = "250000.00"
mvn -pl boleto-emissor -Ddebug=5006 quarkus:dev
```

Exemplo de override da URL do registro em prod:

```powershell
$env:REGISTRO_URL = "http://localhost:8081"
```

## Como subir localmente (sem Docker)

Em terminais separados, na raiz do projeto:

```bash
mvn -pl boleto-registro -Ddebug=5005 quarkus:dev
mvn -pl boleto-emissor -Ddebug=5006 quarkus:dev
mvn -pl boleto-consulta -Ddebug=5007 quarkus:dev
```

As portas de depuração são diferentes para permitir subir os três serviços ao mesmo tempo.

Para demonstrar a API, abra o Swagger UI em `http://localhost:8080/q/swagger-ui`. Ele ajuda a navegar pelos endpoints e schemas. Para chamadas autenticadas com Basic Auth, importe `Central-de-Cobranca.postman_collection.json` no Postman: a coleção já inclui usuários de demonstração e cenários de `201`, `400`, `403`, `409` e consulta do UUID no Registro.

## Build e testes

Na raiz do projeto:

```bash
mvn verify
```

## Health e metrics

Exemplos com portas atuais:

```bash
curl http://localhost:8080/q/health/live
curl http://localhost:8081/q/health/ready
curl http://localhost:8082/q/metrics
```

## Roteiro de demonstracao (banca)

1. Subir `boleto-registro`, `boleto-emissor` e `boleto-consulta`.
2. Verificar operacao basica dos servicos:

```bash
curl http://localhost:8080/status
curl http://localhost:8081/status
curl http://localhost:8082/status
```

3. Validar seguranca no emissor:

```bash
curl -i -X POST http://localhost:8080/boletos -H "Content-Type: application/json" -d "{\"convenio\":\"123456\",\"numeroControleCliente\":\"DEMO-001\",\"valorNominal\":50.00}"
```

Esperado: `401` sem credencial.

4. Emissao com operador (fluxo principal):

```bash
curl -i -u operador:operador123 -X POST http://localhost:8080/boletos -H "Content-Type: application/json" -d "{\"convenio\":\"123456\",\"numeroControleCliente\":\"DEMO-002\",\"valorNominal\":50.00}"
```

Esperado: `201` com identificador.

5. Consulta com auditor:

```bash
curl -i -u auditor:auditor123 http://localhost:8082/boletos/11111111-1111-1111-1111-111111111111
```

Esperado: `200`.

6. Caminho de erro de autorizacao:

```bash
curl -i -u auditor:auditor123 -X POST http://localhost:8080/boletos -H "Content-Type: application/json" -d "{\"convenio\":\"123456\",\"numeroControleCliente\":\"DEMO-003\",\"valorNominal\":50.00}"
```

Esperado: `403`.

7. Caminho de erro de validacao:

```bash
curl -i -u operador:operador123 -X POST http://localhost:8080/boletos -H "Content-Type: application/json" -d "{\"numeroControleCliente\":\"SEM-CONVENIO\",\"valorNominal\":50.00}"
```

Esperado: `400` com `ApiError`.

## Uso critico de IA

O grupo utilizou GitHub Copilot para apoio em analise, implementacao e testes.

Validacoes realizadas antes de aceitar sugestoes:

- revisao de codigo alterado;
- verificacao de contratos e endpoints em OpenAPI/Swagger;
- execucao de testes com Maven (`mvn verify`).

Fato real registrado no processo:

- Durante a etapa de RBAC, houve expectativa inicial de validar sempre corpo `ApiError` no `403` de `@RolesAllowed`.
- Na execucao, observou-se que a camada de seguranca podia devolver `403` diretamente sem corpo JSON padronizado.
- O grupo revisou a expectativa do teste para validar o comportamento real do framework, sem assumir que todo `403` seguiria o mesmo envelope de erro de dominio.

Regra adotada pelo grupo para uso de IA:

- nenhuma sugestao da IA e aceita sem validacao por codigo, documentacao/testes e `mvn verify` verde.
