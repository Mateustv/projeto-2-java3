# Roteiro da banca: passeio pelo código + Postman

**Formato:** walkthrough ao vivo, sem slides  
**Apresentadores:** Pessoa 1 e Pessoa 2  
**Duração planejada:** 11 min 25 s; guardar 35 s de margem para não passar de 12 min

## Antes da banca

1. Na raiz `projeto-final-grupo`, confirme que as portas `8080`, `8081` e `8082` estão livres. Emissor e Registro usam as duas portas exigidas pelo brief; Consulta, serviço adicional, usa `8082`.
2. Em três terminais, suba os serviços nesta ordem:

   ```powershell
   mvn -pl boleto-registro -Ddebug=5005 quarkus:dev
   mvn -pl boleto-emissor -Ddebug=5006 quarkus:dev
   mvn -pl boleto-consulta -Ddebug=5007 quarkus:dev
   ```

3. Abra no VS Code os arquivos indicados abaixo. Deixe as três janelas do terminal visíveis ou organizadas em abas.
4. Importe `Central-de-Cobranca.postman_collection.json` no Postman. A coleção já tem as URLs atuais, credenciais de demonstração e scripts que guardam o `boletoId`.
5. Não rode a coleção inteira durante a fala. Envie os pedidos na ordem deste roteiro; Fault Tolerance será demonstrado por último, pois exige parar e reiniciar o Registro.
6. Tenha a saída do `mvn verify` já aberta: **54 testes, sem falhas**. Não espere o build completo durante os 12 minutos.

## Roteiro cronometrado

| Tempo | Quem | Onde olhar | O que explicar |
|---|---|---|---|
| 0:00–0:20 | Pessoa 1 | [arquitetura.md](arquitetura.md) | “Bom dia. Nosso fluxo é emitir um boleto, registrá-lo e consultar seu estado. Vamos ver o código e provar esse caminho no Postman. O Spring original não está nesta pasta, então a comparação é com os padrões vistos em aula.” |
| 0:20–1:10 | Pessoa 1 | [BoletoResource.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/controllers/BoletoResource.java) | “Este é o ponto de entrada da emissão. No padrão Spring, usaríamos `@RestController` e `@PostMapping`; aqui usamos JAX-RS, com `@Path` e `@POST`. Só o papel `OPERADOR` pode emitir. O recurso chama o serviço e responde `201`, com um `Location` associado ao boleto. A regra de negócio fica no serviço, separada dos detalhes HTTP.” |
| 1:10–2:05 | Pessoa 1 | [EmissaoService.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/EmissaoService.java), [EmissaoRepository.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/repositories/EmissaoRepository.java) | “Na regra da emissão, primeiro verificamos se já existe boleto para esse convênio e número de controle; depois conferimos o limite do valor. A operação é transacional e usa Panache. No Spring Data, poderíamos estender `JpaRepository`; aqui as consultas estão explícitas no repositório. O `flush` ajuda a detectar uma violação de unicidade antes de chamar o outro serviço.” |
| 2:05–3:15 | Pessoa 1 | [RegistroClient.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/clients/RegistroClient.java), [RegistroGateway.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/RegistroGateway.java), [RegistroGatewayTest.java](../boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/RegistroGatewayTest.java) | “Depois de salvar, o Emissor chama o Registro. `RegistroClient` descreve a chamada HTTP e sua autenticação; o gateway concentra as proteções: timeout de 500 milissegundos, duas retentativas, circuit breaker e fallback `503`. Duas retentativas permitem até três chamadas. O UUID repetido é tratado de forma idempotente pelo Registro. No teste, simulamos uma falha e verificamos as tentativas e o rollback da emissão.” |
| 3:15–4:00 | Pessoa 2 | [EmitirBoletoRequest.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/dtos/EmitirBoletoRequest.java), [ApiExceptionHandler.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/handlers/ApiExceptionHandler.java) | “O DTO valida os dados, e o handler transforma as exceções em `ApiError`, com status, código e mensagem. Um campo inválido pode gerar `400`, uma regra de negócio `422` e uma duplicidade `409`. No Spring, esse tratamento costuma ficar em `@RestControllerAdvice`; aqui usamos os mapeadores de exceção do Quarkus.” |
| 4:00–5:10 | Pessoa 2 | [Usuario.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/models/Usuario.java), [UsuariosIniciaisLoader.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/UsuariosIniciaisLoader.java), [BoletoResource.java](../boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/controllers/BoletoResource.java), [ConsultaBoletoResource.java](../boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/controllers/ConsultaBoletoResource.java) | “Os usuários ficam no banco, e a senha é armazenada com BCrypt. No padrão Spring, isso lembra `UserDetailsService` e `BCryptPasswordEncoder`; no Quarkus usamos Security JPA e `BcryptUtil`. `@RolesAllowed` deixa explícito que operador pode emitir e operador ou auditor podem consultar. No Postman veremos o auditor ser recusado ao tentar emitir.” |
| 5:10–5:50 | Pessoa 2 | [application.properties](../boleto-emissor/src/main/resources/application.properties), [ObservabilidadeTest.java](../boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/ObservabilidadeTest.java) | “As configurações variam por perfil: `%dev`, `%test` e `%prod`; algumas também aceitam variáveis de ambiente. Health e métricas ficam em `/q/health` e `/q/metrics`, como recursos operacionais semelhantes ao Actuator do Spring. O teste verifica esse comportamento; no Postman vamos consultar o health.” |
| 5:50–6:15 | Pessoa 2 | [AVALIACAO.md](../AVALIACAO.md), [AutenticacaoBoletoResourceTest.java](../boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/AutenticacaoBoletoResourceTest.java) | “O último `mvn verify` passou com 54 testes: 23 no Emissor, 17 no Registro e 14 na Consulta. Neste teste de autenticação, o grupo validou o comportamento real do `403` e ajustou a expectativa, em vez de confiar numa suposição.” |
| 6:15–10:55 | Ambas | Postman + terminal | “Agora vamos percorrer o fluxo pela API. A Pessoa 1 opera o Postman e a Pessoa 2 narra os resultados, incluindo uma falha controlada do Registro e a recuperação.” |
| 10:55–11:25 | Ambas | README / arquitetura | “Esse é o fluxo principal: emissão, registro e consulta, com validação, segurança e tratamento de falhas. Emissor e Registro estão nas portas `8080` e `8081`, como pede o brief; a Consulta, que é um serviço adicional, está na `8082`. Obrigado.” |

## Postman: sequência para executar ao vivo

Faça os pedidos um de cada vez, clicando em **Send** e mostrando o status e o corpo da resposta. Não use **Run collection** durante a fala. Esta sequência inclui a consulta externa (passo 5) e a falha/recuperação (passo 6) da pasta `09 - Roteiro da Banca` e ocupa 4 min 40 s:

| Tempo | Quem faz | O que fazer no Postman | O que dizer |
|---|---|---|---|
| 6:15–6:25 | Pessoa 1 clica; Pessoa 2 narra | Abra `01 - Health e Observabilidade` → `Emissor - Liveness UP` e clique em **Send**. Mostre `200` e `UP`. | “Primeiro vamos confirmar que o Emissor está rodando. O health respondeu `UP`.” |
| 6:25–6:40 | Pessoa 1 clica; Pessoa 2 narra | Abra `02 - Emissor - Autenticacao e RBAC` → `POST /boletos com auditor -> 403` e clique em **Send**. Mostre `403`. | “A chamada usa o perfil auditor, mas emissão exige operador. A API recusou com `403`.” |
| 6:40–7:25 | Pessoa 1 clica; Pessoa 2 narra | Abra `03 - Emissao - Fluxo Principal` → `Emitir boleto (captura boletoId + valida Location)` e clique em **Send**. Mostre `201`, o corpo, **Headers** com `Location` e os testes da resposta. | “Agora emitimos como operador. A coleção gera um número de controle novo. Recebemos `201`; o corpo traz o identificador e o cabeçalho `Location` aponta para o boleto. O teste da coleção guarda o UUID para a próxima chamada.” |
| 7:25–7:45 | Pessoa 1 clica; Pessoa 2 narra | Abra `04 - Registro - Integracao e Idempotencia` → `GET interno registro por boletoId` e clique em **Send**. Mostre o mesmo UUID e `REGISTRADO`. | “O Registro encontrou o mesmo UUID como `REGISTRADO`. Isso confirma a integração iniciada pelo Emissor.” |
| 7:45–8:10 | Pessoa 1 clica; Pessoa 2 narra | Abra `09 - Roteiro da Banca` → `Passo 5 - Consulta externa no consulta (operador -> 200 com UUID seed)` e clique em **Send**. Mostre `200` e `AGUARDANDO_REGISTRO`. | “Agora consultamos o terceiro serviço usando o UUID de exemplo que já está salvo na coleção. A resposta `200` mostra o boleto no serviço de Consulta.” |
| 8:10–8:30 | Pessoa 1 clica; Pessoa 2 narra | Abra `06 - Validacao e Erros` → `Emissor - campo obrigatorio ausente -> 400` e clique em **Send**. Mostre `400` e `BAD_REQUEST`. | “Este pedido está sem convênio de propósito. A API rejeita os dados com `400` e devolve o erro padronizado.” |
| 8:30–8:40 | Pessoa 1 muda para o terminal do Registro; Pessoa 2 narra | Avise que a falha será controlada. No terminal do `boleto-registro`, pressione **Ctrl+C** para parar somente esse serviço. | “Agora vamos simular a indisponibilidade do serviço Registro. Vou parar somente esse processo e manter Emissor e Consulta rodando.” |
| 8:40–9:00 | Pessoa 1 clica; Pessoa 2 narra | Em `09 - Roteiro da Banca`, abra `Passo 6 - Fault tolerance (registro OFF -> emissor 503)` e clique em **Send**. Mostre `503` e `SERVICE_UNAVAILABLE`. | “O Emissor tentou chamar o Registro e respondeu `503 SERVICE_UNAVAILABLE`. O timeout, as retentativas e o fallback evitam que a falha remota vire uma resposta de sucesso.” |
| 9:00–9:25 | Pessoa 1 reinicia; Pessoa 2 narra | No terminal do Registro, execute `mvn -pl boleto-registro -Ddebug=5005 quarkus:dev` e aguarde a mensagem de inicialização. | “A falha foi controlada; agora estamos subindo o Registro novamente para não deixar o ambiente parado.” |
| 9:25–9:45 | Pessoa 1 clica; Pessoa 2 narra | Abra `07 - Fault Tolerance` → `[POSCONDICAO] Subir boleto-registro e validar recuperacao` e clique em **Send**. Mostre `201`. | “Com o Registro de volta, uma nova emissão é aceita com `201`. Isso confirma que o serviço se recuperou.” |
| 9:45–10:10 | Pessoa 1 mostra os testes; Pessoa 2 fecha | Mostre os testes da coleção que passaram e resuma os resultados. | “Vimos health, autorização, emissão, registro, consulta externa, validação e uma falha real com recuperação. Os testes automatizados também cobrem esses cenários.” |
| 10:10–10:55 | Ambas | Margem para latência ou perguntas; pule esta margem se estiver no horário. | Sem fala planejada. Encerrem a demonstração assim que os passos terminarem para manter o limite de 12 minutos. |

### Antes de começar

- Confirme que os três serviços estão rodando e que o health do Emissor responde. Se algum request falhar por conexão, resolva isso antes de iniciar a apresentação.
- Na coleção, confira as variáveis de URL e as credenciais de demonstração; não é necessário preencher o UUID manualmente.
- A emissão tem um script que gera um novo número de controle e, após o `201`, guarda o UUID em `boletoId`. Se o `GET` seguinte não encontrar o boleto, confira se o teste do request de emissão passou e se `boletoId` foi preenchido.
- Ensaie a parada e a subida do Registro antes da banca. Pare somente o terminal de `boleto-registro`; depois, use `mvn -pl boleto-registro -Ddebug=5005 quarkus:dev` para restaurá-lo e aguarde a confirmação de inicialização.
- O passo 5 usa o UUID de exemplo do serviço de Consulta, não o `boletoId` recém-emitido. O passo 6 demonstra o fallback `503`; use o request de pós-condição de `07 - Fault Tolerance` para provar a recuperação.
- Os requests do passo 6 e da pós-condição geram um número de controle novo automaticamente; não altere os corpos manualmente.
- Não use `Com registro fora do ar, emissao nao persiste localmente (GET interno -> 404)` como prova ao vivo: ele usa `{{boletoId}}`, que pode ainda conter o UUID de uma emissão bem-sucedida anterior. O teste automatizado de Fault Tolerance é a evidência confiável do rollback.
- Se estiver atrasado, pule a validação; mantenha a emissão, a consulta externa e a demonstração `503` com a recuperação do Registro.

**Fault Tolerance altera o estado do ambiente.** Demonstre esse cenário somente no fim, depois das chamadas que dependem do Registro. Pare apenas o terminal de `boleto-registro`, envie o passo 6 para ver `503`, reinicie o serviço e envie a pós-condição de `07 - Fault Tolerance` para ver `201`. Ensaie a parada e a recuperação antes da banca; os testes `RegistroGatewayTest` também cobrem timeout, retentativas, circuito aberto, `502` e `503`.

## Como explicar a migração sem exagerar

Use comparações curtas, ligadas ao arquivo que está na tela:

| Responsabilidade | Padrão Spring | O que aparece neste Quarkus |
|---|---|---|
| Endpoint REST | `@RestController`, `@PostMapping`, `ResponseEntity` | `@Path`, `@POST`, `RestResponse` |
| Injeção e serviços | `@Service`/`@Autowired` | `@ApplicationScoped` e injeção CDI por construtor |
| Repositório JPA | `JpaRepository` e consultas derivadas | `PanacheRepositoryBase`, `find(...)` e `persist(...)` |
| Chamada HTTP | Feign/RestTemplate e Resilience4j | `@RegisterRestClient` e Fault Tolerance MicroProfile |
| Erros REST | `@RestControllerAdvice`/`@ExceptionHandler` | `@ServerExceptionMapper` e `ApiError` |
| Usuários e papéis | Spring Security, `UserDetailsService`, `BCryptPasswordEncoder` | `quarkus-security-jpa`, `@UserDefinition`, `BcryptUtil`, `@RolesAllowed` |
| Configuração e operação | perfis/propriedades e Actuator | `%dev`/`%test`/`%prod`, `/q/health` e `/q/metrics` |
| Testes | `@SpringBootTest`/`@MockBean` | `@QuarkusTest`/`@InjectMock`; para o REST Client, `@InjectMock @RestClient` |

**Limite importante:** o código original Spring não está nesta pasta de trabalho. Essas equivalências vêm dos guias da disciplina e do código Quarkus visível aqui. Se forem afirmar que uma classe específica foi portada ou que uma regra mudou, confirmem isso com o repositório/commit Spring original do grupo antes da banca.

## Frases curtas para perguntas comuns

- **“Por que Panache?”** Reduz código repetitivo de acesso JPA, mas as consultas e transações continuam explícitas.
- **“Por que repetir a chamada?”** Para falhas transitórias; é seguro neste comando porque o Registro trata o UUID repetido de modo idempotente.
- **“O que acontece se o Registro cair?”** O gateway tenta novamente e, depois, retorna `503`; o teste confirma que a emissão local não permanece gravada.
- **“Qual é o custo dessa escolha?”** A chamada é síncrona dentro da transação: isso permite rollback se o Registro falhar, mas mantém a transação aberta durante a chamada de rede. Para um cenário de produção com maior volume, uma outbox seria uma evolução possível.
- **“Como sabemos que os papéis funcionam?”** Os testes e o Postman mostram `401` sem autenticação, `403` para auditor ao emitir e sucesso com operador.
- **“Como organizamos as portas?”** Emissor e Registro usam `8080` e `8081`, como pede o brief; Consulta, serviço adicional, usa `8082`.

