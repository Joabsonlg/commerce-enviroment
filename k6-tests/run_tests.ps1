# Função para executar um teste e salvar os resultados
function Run-Test {
    param (
        [string]$TestName,
        [string]$ScriptPath
    )
    Write-Host "Running $TestName..."
    Write-Host "----------------------------------------"
    
    # Cria diretório para resultados se não existir
    $resultsDir = ".\results\$TestName"
    if (-not (Test-Path $resultsDir)) {
        New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null
    }
    
    # Executa o teste e salva resultados
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $outputFile = Join-Path $resultsDir "${timestamp}_results.json"
    
    k6 run --out json=$outputFile $ScriptPath
    
    Write-Host "Test completed. Results saved to: $outputFile"
    Write-Host "----------------------------------------`n"
}

# Cria diretório principal para resultados
if (-not (Test-Path ".\results")) {
    New-Item -ItemType Directory -Path ".\results" -Force | Out-Null
}

# Executa os testes
Run-Test -TestName "load_test" -ScriptPath ".\scenarios\load_test.js"
Run-Test -TestName "stress_test" -ScriptPath ".\scenarios\stress_test.js"

Write-Host "All tests completed!"
