Set-Location c:/Sistemas/1751-pe-jv-001-main

# Script manual de validacao da Etapa 02 (contratos REST).
#
# O foco aqui e conferir, endpoint por endpoint, se o comportamento HTTP
# implementado bate com o contrato esperado em cada modulo:
# - boleto-emissor  (porta 8080)
# - boleto-registro (porta 8081)
# - boleto-consulta (porta 8082)
#
# Cada linha de resultado imprime:
#   <nome do passo> | esperado=<status> | obtido=<status>
#
# Leitura rapida:
# - esperado == obtido: contrato cumprido.
# - diferente: indica aresta para ajuste no recurso/servico/validacao.

function Show-Result($label, $expected, $actual) {
  Write-Output ("{0} | esperado={1} | obtido={2}" -f $label, $expected, $actual)
}

$authOperador = "Basic " + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("operador:operador123"))
$authAuditor = "Basic " + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("auditor:auditor123"))
$headersOperador = @{ Authorization = $authOperador }
$headersAuditor = @{ Authorization = $authAuditor }

# PASSO 1
# Objetivo: criar um boleto no emissor.
# O teste usa numeroControleCliente dinamico para evitar colisao com execucoes anteriores.
# Esperado: HTTP 201 (criado/aceito no contexto da etapa), com identificador retornado.
# Significado no codigo: regra de validacao basica passou e o recurso foi persistido em memoria.
$payload1 = @{ convenio = '123456'; numeroControleCliente = ('CTRL-' + [guid]::NewGuid().ToString('N').Substring(0,8)); valorNominal = 500.00 } | ConvertTo-Json -Compress
$resp1 = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/boletos' -Headers $headersOperador -ContentType 'application/json' -Body $payload1
$id = $resp1.identificador
Show-Result 'PASSO1 POST emissor' 201 201

# PASSO 2
# Objetivo: validar idempotencia no emissor.
# Reenvia o mesmo payload do passo 1.
# Esperado: HTTP 409 (conflito).
# Significado no codigo: a chave de idempotencia (convenio + numeroControleCliente)
# ja existe no repositorio e o servico bloqueia duplicacao.
try {
  Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/boletos' -Headers $headersOperador -ContentType 'application/json' -Body $payload1 | Out-Null
  Show-Result 'PASSO2 conflito emissor' 409 '2xx'
} catch {
  Show-Result 'PASSO2 conflito emissor' 409 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 3
# Objetivo: validar regra de negocio do valor nominal.
# Envia valor acima do limite definido no servico.
# Esperado: HTTP 422 (unprocessable entity).
# Significado no codigo: payload e sintaticamente valido, mas viola regra de dominio.
$payload3 = @{ convenio='123456'; numeroControleCliente='CTRL-LIMITE'; valorNominal=1000000.00 } | ConvertTo-Json -Compress
try {
  Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/boletos' -Headers $headersOperador -ContentType 'application/json' -Body $payload3 | Out-Null
  Show-Result 'PASSO3 regra emissor' 422 '2xx'
} catch {
  Show-Result 'PASSO3 regra emissor' 422 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 4
# Objetivo: validar erro de entrada obrigatoria ausente.
# O campo convenio nao e enviado.
# Esperado: HTTP 400 (bad request).
# Significado no codigo: Bean Validation detecta violacao de @NotBlank.
$payload4 = @{ numeroControleCliente='SEM-CONVENIO'; valorNominal=50.00 } | ConvertTo-Json -Compress
try {
  Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/boletos' -Headers $headersOperador -ContentType 'application/json' -Body $payload4 | Out-Null
  Show-Result 'PASSO4 payload invalido emissor' 400 '2xx'
} catch {
  Show-Result 'PASSO4 payload invalido emissor' 400 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 5
# Objetivo: garantir que o boleto criado no passo 1 pode ser recuperado no emissor.
# Esperado: HTTP 200.
# Significado no codigo: consulta no repositorio por identificador encontrou o aceite.
$r5 = Invoke-WebRequest -UseBasicParsing -Method Get -Uri "http://localhost:8080/interno/boletos/$id"
Show-Result 'PASSO5 GET interno emissor' 200 ([int]$r5.StatusCode)

# PASSO 6
# Objetivo: registrar o boleto no modulo de registro (primeira chamada).
# Esperado: HTTP 201.
# Significado no codigo: identificador ainda nao existia no repositorio de registro,
# entao foi criado com status REGISTRADO.
$payloadR = @{ identificador = "$id" } | ConvertTo-Json -Compress
$r6 = Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -Headers $headersOperador -ContentType 'application/json' -Body $payloadR
Show-Result 'PASSO6 POST registro' 201 ([int]$r6.StatusCode)

# PASSO 6b
# Objetivo: validar autenticacao obrigatoria no registro.
# Esperado: HTTP 401.
try {
  Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -ContentType 'application/json' -Body $payloadR | Out-Null
  Show-Result 'PASSO6b POST registro sem credencial' 401 '2xx'
} catch {
  Show-Result 'PASSO6b POST registro sem credencial' 401 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 6c
# Objetivo: validar autorizacao por papel no registro.
# Esperado: HTTP 403 para auditor no POST.
try {
  Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -Headers $headersAuditor -ContentType 'application/json' -Body $payloadR | Out-Null
  Show-Result 'PASSO6c POST registro com auditor' 403 '2xx'
} catch {
  Show-Result 'PASSO6c POST registro com auditor' 403 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 7
# Objetivo: reenviar o mesmo comando para validar idempotencia no registro.
# Esperado: HTTP 200.
# Significado no codigo: o registro detecta duplicidade e devolve o recurso existente.
$r7 = Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -Headers $headersOperador -ContentType 'application/json' -Body $payloadR
Show-Result 'PASSO7 POST registro duplicado' 200 ([int]$r7.StatusCode)

# PASSO 8
# Objetivo: consultar o estado do boleto no modulo de registro.
# Esperado: HTTP 200.
# Significado no codigo: confirma que o passo 6 gravou o recurso com sucesso.
$r8 = Invoke-WebRequest -UseBasicParsing -Method Get -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersAuditor
Show-Result 'PASSO8 GET interno registro' 200 ([int]$r8.StatusCode)

# PASSO 9a
# Objetivo: remover o recurso do registro.
# Esperado: HTTP 204 (no content).
# Significado no codigo: delete executado com sucesso sem corpo de resposta.
$r9a = Invoke-WebRequest -UseBasicParsing -Method Delete -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersOperador
Show-Result 'PASSO9a DELETE interno registro' 204 ([int]$r9a.StatusCode)

# PASSO 9aa
# Objetivo: validar autorizacao por papel no DELETE do registro.
# Esperado: HTTP 403 para auditor.
try {
  Invoke-WebRequest -UseBasicParsing -Method Delete -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersAuditor | Out-Null
  Show-Result 'PASSO9aa DELETE registro com auditor' 403 '2xx'
} catch {
  Show-Result 'PASSO9aa DELETE registro com auditor' 403 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 9b
# Objetivo: confirmar efeito do delete.
# Esperado: HTTP 404 na consulta subsequente.
# Significado no codigo: recurso realmente removido do repositorio.
try {
  Invoke-WebRequest -UseBasicParsing -Method Get -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersAuditor | Out-Null
  Show-Result 'PASSO9b GET apos delete' 404 '2xx'
} catch {
  Show-Result 'PASSO9b GET apos delete' 404 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 10
# Objetivo: validar endpoint de consulta com dado semeado de referencia.
# Esperado: HTTP 200.
# Significado no codigo: fluxo de consulta publica esta respondendo para um UUID valido existente.
$r10 = Invoke-WebRequest -UseBasicParsing -Method Get -Uri 'http://localhost:8082/boletos/11111111-1111-1111-1111-111111111111' -Headers $headersOperador
Show-Result 'PASSO10 GET consulta semeado' 200 ([int]$r10.StatusCode)

# PASSO 11
# Objetivo: validar tratamento de identificador invalido na consulta.
# Esperado: HTTP 400.
# Significado no codigo: parser de UUID falha e a excecao de identificador invalido e mapeada.
try {
  Invoke-WebRequest -UseBasicParsing -Method Get -Uri 'http://localhost:8082/boletos/abc' -Headers $headersOperador | Out-Null
  Show-Result 'PASSO11 GET consulta id invalido' 400 '2xx'
} catch {
  Show-Result 'PASSO11 GET consulta id invalido' 400 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 12
# Objetivo: validar caso de recurso inexistente na consulta.
# Esperado: HTTP 404.
# Significado no codigo: UUID valido, mas sem correspondencia no repositorio.
try {
  Invoke-WebRequest -UseBasicParsing -Method Get -Uri 'http://localhost:8082/boletos/22222222-2222-2222-2222-222222222222' -Headers $headersAuditor | Out-Null
  Show-Result 'PASSO12 GET consulta nao encontrado' 404 '2xx'
} catch {
  Show-Result 'PASSO12 GET consulta nao encontrado' 404 ([int]$_.Exception.Response.StatusCode)
}
