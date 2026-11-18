# Etapa 02 - Plano de testes dos endpoints

## Objetivo
Validar todos os endpoints criados na etapa 02, cobrindo caminho feliz e erros basicos de contrato HTTP.

## Escopo
- Inclui: status, recursos REST da etapa 02 e publicacao OpenAPI.
- Inclui: codigos 200, 201, 204, 400, 404, 409, 422.
- Exclui por enquanto: integracao real automatizada entre modulos via chamada HTTP interna (ainda nao implementada na aplicacao).

## Matriz de testes

### boleto-emissor
- `GET /status` -> 200
- `POST /boletos` -> 201 (criacao)
- `POST /boletos` repetido mesma chave -> 409
- `POST /boletos` violando regra de negocio -> 422
- `POST /boletos` payload invalido -> 400
- `POST /boletos` JSON malformado -> 400
- `GET /interno/boletos/{id}` com id criado -> 200
- `GET /interno/boletos/{id}` inexistente -> 404
- `GET /q/openapi` -> 200

### boleto-registro
- `GET /status` -> 200
- `POST /interno/comandos/registro` novo -> 201
- `POST /interno/comandos/registro` duplicado -> 200
- `POST /interno/comandos/registro` invalido -> 400
- `GET /interno/boletos/{id}` inexistente -> 404
- `DELETE /interno/boletos/{id}` existente -> 204
- `DELETE /interno/boletos/{id}` inexistente -> 404
- `GET /q/openapi` -> 200

### boleto-consulta
- `GET /status` -> 200
- `GET /boletos/{id}` valido existente -> 200
- `GET /boletos/{id}` formato invalido -> 400
- `GET /boletos/{id}` inexistente -> 404
- `GET /q/openapi` -> 200

## Interacao entre modulos: status atual
Ainda nao ha chamada HTTP entre modulos implementada no codigo da etapa 02 (sem REST Client entre os servicos).

Conclusao:
- Ja e possivel testar contratos de cada modulo de forma completa e confiavel com QuarkusTest.
- A interacao real modulo-a-modulo deve ser automatizada em etapa futura, quando a integracao entre servicos existir (ex.: REST Client no fluxo emissor -> registro e consulta agregando fontes).
