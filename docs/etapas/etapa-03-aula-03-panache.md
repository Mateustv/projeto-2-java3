# Etapa 03 - Aula 03 - Persistencia com Panache

## Status
Concluida.

## Objetivo
Implantar persistencia de dominio com Hibernate ORM + Panache e transacoes.

## Escopo
- Incluir: entidades, repositorios, transacoes e regras de unicidade.
- Excluir: integracao resiliente e politicas completas de seguranca.

## Tarefas tecnicas
1. Criar entidades por contexto de servico.
2. Implementar repositorios/queries com Panache.
3. Aplicar transacao nas operacoes de escrita.
4. Tratar casos de conflito e idempotencia essencial.

## Checklist de implementacao
- [x] Dependencias `quarkus-hibernate-orm-panache` e `quarkus-jdbc-h2` adicionadas nos 3 modulos.
- [x] Configuracao H2 por modulo para `dev` e `test`.
- [x] Entidades JPA criadas por contexto de servico.
- [x] Repositorios migrados de memoria para Panache (`PanacheRepositoryBase<Entidade, UUID>`).
- [x] Escritas com transacao (`@Transactional`) nos servicos de emissao e registro.
- [x] Regra de unicidade de idempotencia no emissor com constraint de banco.
- [x] Semeadura do modulo de consulta mantida no startup para o UUID de referencia.

## Criterios de saida
- Dados persistem e sao consultados corretamente.
- Testes de persistencia e regra de negocio passam.

## Criterios do brief cobertos
- C3 (persistencia com Panache), C7.

## Evidencias esperadas
- entidades e repositorios Panache.
- testes de persistencia e conflitos.
- configuracao H2 funcional para dev/test.

## Evidencias implementadas

### Arquivos de infraestrutura (dependencias + config)
- `boleto-emissor/pom.xml`
- `boleto-registro/pom.xml`
- `boleto-consulta/pom.xml`
- `boleto-emissor/src/main/resources/application.properties`
- `boleto-registro/src/main/resources/application.properties`
- `boleto-consulta/src/main/resources/application.properties`

### Entidades JPA por servico
- `boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/models/BoletoEmissaoEntity.java`
- `boleto-registro/src/main/java/br/com/caixa/cobranca/registro/models/BoletoRegistroEntity.java`
- `boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/models/BoletoConsultaEntity.java`

### Repositorios Panache
- `boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/repositories/EmissaoRepository.java`
- `boleto-registro/src/main/java/br/com/caixa/cobranca/registro/repositories/RegistroRepository.java`
- `boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/repositories/ConsultaRepository.java`

### Servicos com transacao
- `boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/EmissaoService.java`
- `boleto-registro/src/main/java/br/com/caixa/cobranca/registro/services/RegistroBoletoService.java`

## Evidencia de validacao (execucao)
Comando executado na raiz do projeto:

```bash
mvn test -DskipITs
```

Resultado obtido:
- `BUILD SUCCESS`
- `boleto-emissor`: 9 testes, 0 falhas, 0 erros
- `boleto-registro`: 9 testes, 0 falhas, 0 erros
- `boleto-consulta`: 5 testes, 0 falhas, 0 erros

Observacao operacional:
- Em ambiente Windows com `quarkus:dev` ativo, `mvn clean ...` pode falhar por lock de arquivo em `target`.
- Para validar enquanto os servicos estao em execucao, preferir `mvn test -DskipITs`.
