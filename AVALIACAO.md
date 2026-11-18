# Auto-avaliacao - Central de Cobranca Quarkus

Grupo: Mateus Tavares D. de Araujo, Vinicius Morais Murta, Mikael Viana Ferreira
Tema/domínio: emissao, registro e consulta de boletos com integracao sincrona resiliente e seguranca por RBAC
Versoes: Quarkus 3.15.7 · Java 21

## Evidências por critério

1. Serviços e API REST - nível auto-atribuído: 2
   Evidência:
    - Estrutura multi-modulo: [pom.xml](pom.xml)
    - Recursos REST e codigos HTTP coerentes:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/controllers/BoletoResource.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/controllers/BoletoResource.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/controllers/RegistroComandoResource.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/controllers/RegistroComandoResource.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/controllers/RegistroConsultaInternaResource.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/controllers/RegistroConsultaInternaResource.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/controllers/ConsultaBoletoResource.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/controllers/ConsultaBoletoResource.java)
    - OpenAPI habilitado: endpoints /q/openapi dos 3 modulos
      Testes que comprovam:
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoResourceTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoResourceTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ConsultaBoletoResourceTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ConsultaBoletoResourceTest.java)
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/OpenApiResourceTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/OpenApiResourceTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/OpenApiResourceTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/OpenApiResourceTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/OpenApiResourceTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/OpenApiResourceTest.java)

2. Integração entre serviços e tolerância a falhas - nível auto-atribuído: 2
   Evidência:
    - REST Client e mapeamento de erro remoto:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/clients/RegistroClient.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/clients/RegistroClient.java)
    - FT (timeout, retry, circuit breaker, fallback):
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/RegistroGateway.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/RegistroGateway.java)
    - Configuracao de FT e URL por ambiente:
        - [boleto-emissor/src/main/resources/application.properties](boleto-emissor/src/main/resources/application.properties)
    - Idempotencia do registro por UUID:
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/services/RegistroBoletoService.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/services/RegistroBoletoService.java)
          Testes que comprovam:
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/RegistroGatewayTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/RegistroGatewayTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java)

3. Persistência com Panache - nível auto-atribuído: 2
   Evidência:
    - Entidades JPA:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/models/BoletoEmissaoEntity.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/models/BoletoEmissaoEntity.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/models/BoletoRegistroEntity.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/models/BoletoRegistroEntity.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/models/BoletoConsultaEntity.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/models/BoletoConsultaEntity.java)
    - Repositorios Panache:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/repositories/EmissaoRepository.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/repositories/EmissaoRepository.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/repositories/RegistroRepository.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/repositories/RegistroRepository.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/repositories/ConsultaRepository.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/repositories/ConsultaRepository.java)
    - Transacoes em escrita:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/EmissaoService.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/EmissaoService.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/services/RegistroBoletoService.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/services/RegistroBoletoService.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/repositories/ConsultaRepository.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/repositories/ConsultaRepository.java)
          Testes que comprovam:
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoResourceTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoResourceTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ConsultaBoletoResourceTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ConsultaBoletoResourceTest.java)

4. Validação e tratamento de erros - nível auto-atribuído: 2
   Evidência:
    - Bean Validation em DTOs:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/dtos/EmitirBoletoRequest.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/dtos/EmitirBoletoRequest.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/dtos/RegistrarBoletoCommand.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/dtos/RegistrarBoletoCommand.java)
    - Envelope padronizado ApiError:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/dtos/ApiError.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/dtos/ApiError.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/dtos/ApiError.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/dtos/ApiError.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/dtos/ApiError.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/dtos/ApiError.java)
    - Exception handlers globais:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/handlers/ApiExceptionHandler.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/handlers/ApiExceptionHandler.java)
        - [boleto-registro/src/main/java/br/com/caixa/cobranca/registro/handlers/ApiExceptionHandler.java](boleto-registro/src/main/java/br/com/caixa/cobranca/registro/handlers/ApiExceptionHandler.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/handlers/ApiExceptionHandler.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/handlers/ApiExceptionHandler.java)
          Testes que comprovam:
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoPayloadInvalidoTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoPayloadInvalidoTest.java)
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoResourceTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/BoletoResourceTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroPayloadInvalidoTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroPayloadInvalidoTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/RegistroResourceTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ConsultaBoletoResourceTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ConsultaBoletoResourceTest.java)

5. Segurança - nível auto-atribuído: 2
   Evidência:
    - Security JPA e usuario em banco:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/models/Usuario.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/models/Usuario.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/models/Usuario.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/models/Usuario.java)
    - BCrypt e seed de usuarios:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/UsuariosIniciaisLoader.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/UsuariosIniciaisLoader.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/services/UsuariosIniciaisLoader.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/services/UsuariosIniciaisLoader.java)
    - RBAC com papeis operador/auditor:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/controllers/BoletoResource.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/controllers/BoletoResource.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/controllers/ConsultaBoletoResource.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/controllers/ConsultaBoletoResource.java)
    - Constantes de papeis:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/security/SecurityRoles.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/security/SecurityRoles.java)
        - [boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/security/SecurityRoles.java](boleto-consulta/src/main/java/br/com/caixa/cobranca/consulta/security/SecurityRoles.java)
          Testes que comprovam:
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/AutenticacaoBoletoResourceTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/AutenticacaoBoletoResourceTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/AutenticacaoConsultaBoletoResourceTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/AutenticacaoConsultaBoletoResourceTest.java)

6. Config, health e métricas - nível auto-atribuído: 2
   Evidência:
    - Perfis e propriedades:
        - [boleto-emissor/src/main/resources/application.properties](boleto-emissor/src/main/resources/application.properties)
        - [boleto-registro/src/main/resources/application.properties](boleto-registro/src/main/resources/application.properties)
        - [boleto-consulta/src/main/resources/application.properties](boleto-consulta/src/main/resources/application.properties)
    - Metrica de negocio no emissor:
        - [boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/EmissaoService.java](boleto-emissor/src/main/java/br/com/caixa/cobranca/emissor/services/EmissaoService.java)
          Testes que comprovam:
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/ObservabilidadeTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/ObservabilidadeTest.java)
    - [boleto-registro/src/test/java/br/com/caixa/cobranca/registro/ObservabilidadeTest.java](boleto-registro/src/test/java/br/com/caixa/cobranca/registro/ObservabilidadeTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ObservabilidadeTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/ObservabilidadeTest.java)
    - [boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/LimiteEmissaoConfigTest.java](boleto-emissor/src/test/java/br/com/caixa/cobranca/emissor/LimiteEmissaoConfigTest.java)
    - [boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/DadosExemploConfigTest.java](boleto-consulta/src/test/java/br/com/caixa/cobranca/consulta/DadosExemploConfigTest.java)

7. Testes - nível auto-atribuído: 2
   Evidência:
    - Suite com @QuarkusTest nos 3 modulos para criterios 1 a 6, incluindo caminhos de erro
    - Ultimo `mvn verify` (raiz) verde
    - Contagem real observada no ultimo verify:
        - boleto-emissor: 23 testes
        - boleto-registro: 17 testes
        - boleto-consulta: 14 testes
        - total: 54 testes

8. Uso crítico de IA - nível auto-atribuído: 2
   Como usamos IA e o que validamos manualmente:
    - GitHub Copilot foi usado para analise, implementacao e testes.
    - Todas as sugestoes foram validadas com revisao de codigo, consulta de contratos OpenAPI/Swagger e execucao de Maven.
    - Caso concreto: no RBAC, a expectativa inicial de validar corpo ApiError em todo 403 de @RolesAllowed foi revisada apos execucao real mostrar que a camada de seguranca pode retornar 403 direto; o teste foi ajustado para refletir o comportamento observado.
    - Regra adotada: sugestao de IA so e aceita apos validacao por codigo, documentacao/testes e `mvn verify` verde.

9. Execução - nível auto-atribuído: 2
   Evidência:
    - Instrucoes de subida e validacao em [README.md](README.md)
    - Arquitetura e fluxos em [docs/arquitetura.md](docs/arquitetura.md)
    - Projeto executa e testa com `mvn verify` na raiz
      Observacao de aderencia ao descritor:
    - Os dois servicos do fluxo principal, Emissor e Registro, usam as portas exigidas 8080 e 8081.
    - O servico adicional de Consulta usa a porta 8082.

## Opcionais entregues

Nao se aplica (grupo com 3 integrantes; sem obrigatoriedade de opcionais).
