# Etapa 04 - Aula 04 - Configuracao, Health e Metrics

## Status
Concluida.

## Objetivo
Padronizar configuracao por perfil e disponibilizar observabilidade basica em todos os servicos.

## Escopo
- Incluir: properties por ambiente, health checks e metricas.
- Excluir: ajuste fino de performance e tuning avancado.

## Tarefas tecnicas
- [x] Organizar configuracoes por perfil (dev/test/prod), com datasource H2 separado por perfil.
- [x] Externalizar o limite nominal do boleto e a carga dos dados de exemplo.
- [x] Habilitar e testar `/q/health/live` e `/q/health/ready` nos tres servicos.
- [x] Habilitar e testar `/q/metrics` nos tres servicos.
- [x] Expor e testar uma metrica de negocio no emissor.

## Criterios de saida
- Endpoints de health e metrics respondem em todos os servicos.
- Configuracoes criticas externalizadas.

## Criterios do brief cobertos
- C6 (configuracao, health e metricas), C7.

## Evidencias esperadas
- arquivos de configuracao por perfil.
- resposta de /q/health e /q/metrics documentada.
- testes de configuracao e disponibilidade.

## Evidencias implementadas
- `boleto-emissor/src/main/resources/application.properties`: limite `cobranca.boleto.valor-maximo`, com sobrescrita por `COBRANCA_BOLETO_VALOR_MAXIMO` em dev/prod.
- `boleto-consulta/src/main/resources/application.properties`: carga do boleto de exemplo habilitada em dev/test e desabilitada em prod.
- Os tres `pom.xml` incluem SmallRye Health e Micrometer Prometheus.
- Testes `ObservabilidadeTest` cobrem health e metricas nos tres modulos; `LimiteEmissaoConfigTest` prova a alteracao do limite por perfil de teste; `DadosExemploConfigTest` prova que a carga de exemplo pode ser desabilitada.
- `%prod` continua usando H2 em memoria no escopo didatico; isso nao representa uma configuracao de banco de producao.
- Validacao: `mvn -B verify` na raiz do projeto — `BUILD SUCCESS`, 31 testes, 0 falhas e 0 erros.
