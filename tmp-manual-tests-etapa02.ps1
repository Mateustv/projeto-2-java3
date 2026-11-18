Set-Location c:/Sistemas/1751-pe-jv-001-main

$operador = 'operador:operador123'
$auditor = 'auditor:auditor123'

Write-Output '--- PASSO 1: criar boleto ---'
$resp = curl.exe -s -u $operador -X POST "http://localhost:8080/boletos" -H "Content-Type: application/json" --data-raw '{"convenio":"123456","numeroControleCliente":"CTRL-001","valorNominal":500.00}'
$resp
$id = ($resp | ConvertFrom-Json).identificador
Write-Output "ID=$id"

Write-Output '--- PASSO 2: conflito idempotencia ---'
curl.exe -i -u $operador -X POST "http://localhost:8080/boletos" -H "Content-Type: application/json" --data-raw '{"convenio":"123456","numeroControleCliente":"CTRL-001","valorNominal":500.00}'

Write-Output '--- PASSO 3: erro regra ---'
curl.exe -i -u $operador -X POST "http://localhost:8080/boletos" -H "Content-Type: application/json" --data-raw '{"convenio":"123456","numeroControleCliente":"CTRL-LIMITE","valorNominal":1000000.00}'

Write-Output '--- PASSO 4: payload invalido ---'
curl.exe -i -u $operador -X POST "http://localhost:8080/boletos" -H "Content-Type: application/json" --data-raw '{"numeroControleCliente":"SEM-CONVENIO","valorNominal":50.00}'

Write-Output '--- PASSO 5: consulta interna emissor ---'
curl.exe -i "http://localhost:8080/interno/boletos/$id"

Write-Output '--- PASSO 6: registro comando 1 ---'
curl.exe -i -u $operador -X POST "http://localhost:8081/interno/comandos/registro" -H "Content-Type: application/json" --data-raw "{`"identificador`":`"$id`"}"

Write-Output '--- PASSO 7: registro comando duplicado ---'
curl.exe -i -u $operador -X POST "http://localhost:8081/interno/comandos/registro" -H "Content-Type: application/json" --data-raw "{`"identificador`":`"$id`"}"

Write-Output '--- PASSO 8: consulta interna registro ---'
curl.exe -i -u $auditor "http://localhost:8081/interno/boletos/$id"

Write-Output '--- PASSO 9a: remover registro ---'
curl.exe -i -u $operador -X DELETE "http://localhost:8081/interno/boletos/$id"

Write-Output '--- PASSO 9b: consulta apos remover ---'
curl.exe -i -u $auditor "http://localhost:8081/interno/boletos/$id"

Write-Output '--- PASSO 10: consulta semeado ---'
curl.exe -i -u $operador "http://localhost:8082/boletos/11111111-1111-1111-1111-111111111111"

Write-Output '--- PASSO 11: identificador invalido ---'
curl.exe -i -u $operador "http://localhost:8082/boletos/abc"

Write-Output '--- PASSO 12: nao encontrado ---'
curl.exe -i -u $auditor "http://localhost:8082/boletos/22222222-2222-2222-2222-222222222222"
