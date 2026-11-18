# Etapa 06 - Aula 06 - Validacao e Erros Padronizados

## Status
Concluida nos tres servicos.

## Objetivo
Padronizar validacao de entrada e resposta de erro entre os 3 servicos.

## Escopo
- Incluir: Bean Validation e mapeamento global de excecoes.
- Excluir: mudancas de contrato nao planejadas na etapa 02.

## Tarefas tecnicas
1. Aplicar @Valid e constraints nos DTOs.
2. Criar modelo de erro padrao.
3. Mapear excecoes de dominio e tecnicas para HTTP coerente.
4. Cobrir erros de entrada, conflito e indisponibilidade.

## Criterios de saida
- Respostas de erro seguem formato unico.
- Casos 400/404/409/422/503 cobertos por testes.

## Criterios do brief cobertos
- C4 (validacao e tratamento de erros), C7.

## Evidencias esperadas
- DTOs validados.
- exception mappers globais.
- testes de caminho de erro por endpoint critico.

## Implementacao concluida
- Os DTOs de entrada do emissor e do registro validam campos obrigatorios, positivos e precisao numerica com mensagens em portugues.
- Os tres servicos respondem erros no formato `status`, `codigo` e `mensagem`, sem expor stack trace, SQL ou detalhes internos.
- Excecoes de dominio mantem os status definidos pelo contrato; erros de validacao e JSON malformado respondem `400`.
- Erros do framework, como rota inexistente e metodo nao permitido, mantem o status HTTP e recebem o envelope padrao.
- Erros inesperados sao registrados no log e retornam `500` com mensagem segura.

## Testes executados
- `boleto-emissor`: validacao, JSON malformado, rota inexistente (`404`), metodo nao permitido (`405`), conflitos (`409`), regra de negocio (`422`) e indisponibilidade/integracao (`502`/`503`).
- `boleto-registro`: validacao, JSON malformado, consulta e remocao de recurso inexistente (`404`).
- `boleto-consulta`: identificador invalido (`400`) e recurso inexistente (`404`).
- Resultado: 37 testes nos tres modulos, sem falhas.
