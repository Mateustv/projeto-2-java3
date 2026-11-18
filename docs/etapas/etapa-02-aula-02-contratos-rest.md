# Etapa 02 - Aula 02 - Contratos REST, CDI e OpenAPI

## Objetivo
Definir contratos REST dos 3 servicos com semantica HTTP consistente e estrutura de camadas com CDI.

## Escopo
- Incluir: endpoints principais, DTOs, codigos de status, OpenAPI.
- Excluir: persistencia completa e seguranca final.

## Tarefas tecnicas
1. Definir recursos por servico e operacoes principais.
2. Organizar camadas recurso -> servico -> repositorio.
3. Publicar OpenAPI e revisar contratos.
4. Cobrir status essenciais (200/201/204/400/404/409/422).

## Criterios de saida
- Contratos dos endpoints documentados e executaveis.
- Testes de endpoint cobrindo caminho feliz e erro basico.

## Criterios do brief cobertos
- C1 (servicos e API REST), base para C4, C7.

## Evidencias esperadas
- recursos REST por modulo.
- schema OpenAPI acessivel.
- testes de endpoint com asserts de status.
