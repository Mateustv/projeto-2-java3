# Etapa 01 - Aula 01 - Fundacao Quarkus

## Objetivo
Criar a base do projeto multi-modulo Quarkus mantendo 3 servicos: boleto-emissor, boleto-registro e boleto-consulta.

## Escopo
- Incluir: agregador Maven, padrao de versao Java/Quarkus, estrutura inicial dos 3 modulos.
- Excluir: regras de negocio completas e integracao resiliente.

## Tarefas tecnicas
1. Criar/ajustar pom agregador com modulos.
2. Configurar cada servico Quarkus com build funcional.
3. Definir portas locais e perfis basicos de execucao.
4. Adicionar smoke test inicial com QuarkusTest.

## Criterios de saida
- Build na raiz executa sem erro.
- 3 servicos sobem localmente sem Docker.
- Estrutura pronta para evoluir nas aulas seguintes.

## Criterios do brief cobertos
- C1 (base de servicos REST), C7 (base de testes), C9 (execucao local).

## Evidencias esperadas
- poms atualizados.
- classe(s) de recurso inicial.
- teste smoke passando.
- instrucoes iniciais no README.
