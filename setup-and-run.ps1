# Script para iniciar PostgreSQL e rodar a aplicação RADAR
# Execute: .\setup-and-run.ps1

Write-Host "=== RADAR - Setup PostgreSQL e Aplicação ===" -ForegroundColor Cyan

# Verificar se Docker está rodando
Write-Host "`n1. Verificando Docker..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "   ✓ Docker está rodando" -ForegroundColor Green
    
    Write-Host "`n2. Iniciando PostgreSQL e PgAdmin..." -ForegroundColor Yellow
    docker-compose up -d
    
    Write-Host "`n3. Aguardando PostgreSQL iniciar (10 segundos)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
} catch {
    Write-Host "   ✗ Docker não está rodando!" -ForegroundColor Red
    Write-Host "`nPor favor:" -ForegroundColor Yellow
    Write-Host "1. Inicie o Docker Desktop" -ForegroundColor White
    Write-Host "2. Execute novamente este script" -ForegroundColor White
    Write-Host "`nOu configure PostgreSQL manualmente:" -ForegroundColor Yellow
    Write-Host "- Instale PostgreSQL 16" -ForegroundColor White
    Write-Host "- Crie database: radar" -ForegroundColor White
    Write-Host "- Crie usuário: radar / senha: radar123" -ForegroundColor White
    Write-Host "`nPressione qualquer tecla para sair..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host "`n4. Compilando aplicação..." -ForegroundColor Yellow
.\mvnw.cmd clean package -DskipTests -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Compilação bem-sucedida" -ForegroundColor Green
} else {
    Write-Host "   ✗ Erro na compilação" -ForegroundColor Red
    exit 1
}

Write-Host "`n5. Iniciando aplicação Spring Boot..." -ForegroundColor Yellow
Write-Host "`nAplicação iniciará em: http://localhost:9090" -ForegroundColor Cyan
Write-Host "Swagger UI: http://localhost:9090/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host "PgAdmin: http://localhost:5050 (admin@radar.com / admin123)" -ForegroundColor Cyan
Write-Host "`nPressione Ctrl+C para parar a aplicação`n" -ForegroundColor Gray

.\mvnw.cmd spring-boot:run
