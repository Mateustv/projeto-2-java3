# Etapa 07 - Aula 07 - Autenticacao com Banco e BCrypt

## Objetivo
Implementar autenticacao com usuarios persistidos e senha com hash BCrypt.

## Escopo
- Incluir: quarkus-security-jpa, entidade de usuario e fluxo de autenticacao.
- Excluir: politicas completas de autorizacao por papel (etapa 08).

## Tarefas tecnicas
1. Criar modelo de usuario/credencial no banco.
2. Configurar security-jpa para autenticacao.
3. Garantir armazenamento de senha com BCrypt.
4. Incluir usuarios de teste para ambiente local.

## Criterios de saida
- Credencial valida autentica com sucesso.
- Credencial ausente/invalida retorna erro adequado.

## Criterios do brief cobertos
- C5 (autenticacao), C7.

## Evidencias esperadas
- entidades/tabelas de usuario.
- configuracao de seguranca funcional.
- testes de autenticacao positiva e negativa.
