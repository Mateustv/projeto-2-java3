Set-Location c:/Sistemas/1751-pe-jv-001-main

# Script manual de validacao da Etapa 03 (persistencia Panache + transacoes).
#
# Este roteiro valida comportamentos que exercitam escrita/leitura no banco H2
# com repositorios Panache e regras de idempotencia/conflito.
#
# Modulos/alvos:
# - boleto-emissor  (porta 8080)
# - boleto-registro (porta 8081)
# - boleto-consulta (porta 8082)
#
# Cada linha imprime:
#   <nome do passo> | esperado=<status> | obtido=<status>

function Show-Result($label, $expected, $actual) {
  Write-Output ("{0} | esperado={1} | obtido={2}" -f $label, $expected, $actual)
}

$authOperador = "Basic " + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("operador:operador123"))
$authAuditor = "Basic " + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("auditor:auditor123"))
$headersOperador = @{ Authorization = $authOperador }
$headersAuditor = @{ Authorization = $authAuditor }

# PASSO 1
# Criar boleto no emissor para gerar registro persistido.
$payload1 = @{
  convenio = '123456'
  numeroControleCliente = ('CTRL-PANACHE-' + [guid]::NewGuid().ToString('N').Substring(0,8))
  valorNominal = 321.45
} | ConvertTo-Json -Compress

$resp1 = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/boletos' -Headers $headersOperador -ContentType 'application/json' -Body $payload1
$id = $resp1.identificador
Show-Result 'PASSO1 POST emissor (persistencia)' 201 201

# PASSO 2
# Repetir mesmo payload no emissor para validar unicidade/idempotencia no banco.
try {
  Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8080/boletos' -Headers $headersOperador -ContentType 'application/json' -Body $payload1 | Out-Null
  Show-Result 'PASSO2 idempotencia emissor' 409 '2xx'
} catch {
  Show-Result 'PASSO2 idempotencia emissor' 409 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 3
# Buscar no endpoint interno do emissor para confirmar leitura apos escrita.
$r3 = Invoke-WebRequest -UseBasicParsing -Method Get -Uri "http://localhost:8080/interno/boletos/$id"
Show-Result 'PASSO3 GET interno emissor' 200 ([int]$r3.StatusCode)

# PASSO 4
# Registrar no modulo de registro (escrita transacional).
$payloadR = @{ identificador = "$id" } | ConvertTo-Json -Compress
$r4 = Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -Headers $headersOperador -ContentType 'application/json' -Body $payloadR
Show-Result 'PASSO4 POST registro (novo)' 201 ([int]$r4.StatusCode)

# PASSO 4b
# POST sem credencial deve exigir autenticacao.
try {
  Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -ContentType 'application/json' -Body $payloadR | Out-Null
  Show-Result 'PASSO4b POST registro sem credencial' 401 '2xx'
} catch {
  Show-Result 'PASSO4b POST registro sem credencial' 401 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 4c
# Auditor nao pode registrar/remover no registro.
try {
  Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -Headers $headersAuditor -ContentType 'application/json' -Body $payloadR | Out-Null
  Show-Result 'PASSO4c POST registro com auditor' 403 '2xx'
} catch {
  Show-Result 'PASSO4c POST registro com auditor' 403 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 5
# Repetir comando de registro para validar idempotencia no repositorio Panache.
$r5 = Invoke-WebRequest -UseBasicParsing -Method Post -Uri 'http://localhost:8081/interno/comandos/registro' -Headers $headersOperador -ContentType 'application/json' -Body $payloadR
Show-Result 'PASSO5 POST registro (duplicado)' 200 ([int]$r5.StatusCode)

# PASSO 6
# Consultar no registro para confirmar persistencia da escrita.
$r6 = Invoke-WebRequest -UseBasicParsing -Method Get -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersAuditor
Show-Result 'PASSO6 GET interno registro' 200 ([int]$r6.StatusCode)

# PASSO 7
# Remover no registro e confirmar efeito da transacao.
$r7 = Invoke-WebRequest -UseBasicParsing -Method Delete -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersOperador
Show-Result 'PASSO7 DELETE interno registro' 204 ([int]$r7.StatusCode)

try {
  Invoke-WebRequest -UseBasicParsing -Method Delete -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersAuditor | Out-Null
  Show-Result 'PASSO7b DELETE registro com auditor' 403 '2xx'
} catch {
  Show-Result 'PASSO7b DELETE registro com auditor' 403 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 8
# Confirmar que o item removido nao esta mais disponivel.
try {
  Invoke-WebRequest -UseBasicParsing -Method Get -Uri "http://localhost:8081/interno/boletos/$id" -Headers $headersAuditor | Out-Null
  Show-Result 'PASSO8 GET apos delete registro' 404 '2xx'
} catch {
  Show-Result 'PASSO8 GET apos delete registro' 404 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 9
# Validar dado semeado na consulta (startup + persistencia da entidade base).
$r9 = Invoke-WebRequest -UseBasicParsing -Method Get -Uri 'http://localhost:8082/boletos/11111111-1111-1111-1111-111111111111' -Headers $headersOperador
Show-Result 'PASSO9 GET consulta semeado' 200 ([int]$r9.StatusCode)

# PASSO 10
# Validar caso nao encontrado na consulta.
try {
  Invoke-WebRequest -UseBasicParsing -Method Get -Uri 'http://localhost:8082/boletos/22222222-2222-2222-2222-222222222222' -Headers $headersAuditor | Out-Null
  Show-Result 'PASSO10 GET consulta nao encontrado' 404 '2xx'
} catch {
  Show-Result 'PASSO10 GET consulta nao encontrado' 404 ([int]$_.Exception.Response.StatusCode)
}

# PASSO 11
# Validar identificador invalido no endpoint de consulta.
try {
  Invoke-WebRequest -UseBasicParsing -Method Get -Uri 'http://localhost:8082/boletos/abc' -Headers $headersOperador | Out-Null
  Show-Result 'PASSO11 GET consulta id invalido' 400 '2xx'
} catch {
  Show-Result 'PASSO11 GET consulta id invalido' 400 ([int]$_.Exception.Response.StatusCode)
}

Write-Output "ETAPA03-CHECK finalizado | idTeste=$id"
