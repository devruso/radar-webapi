<#
run-dev.ps1

Usage:
  # Start app (no DB creation)
  .\run-dev.ps1

  # Create DB/user (requires superuser credentials) and start app
  .\run-dev.ps1 -CreateDb

Environment variables used (defaults shown):
  DB_URL                jdbc:postgresql://localhost:5432/radar
  DB_USERNAME           radar_user
  DB_PASSWORD           radar_pass
  DB_INIT               false
  INIT_SQL              sql/populate_data.sql

  When using -CreateDb, also set:
  PG_SUPERUSER          postgres
  PG_SUPERPASS          (password for superuser)
  PG_HOST               localhost
  PG_PORT               5432
#>

param(
    [switch]$CreateDb
)

Set-StrictMode -Version Latest

# Defaults
$DB_NAME = 'radar'
$env:DB_URL = $env:DB_URL -or "jdbc:postgresql://localhost:5432/$DB_NAME"
$env:DB_USERNAME = $env:DB_USERNAME -or 'radar_user'
$env:DB_PASSWORD = $env:DB_PASSWORD -or 'radar_pass'
$env:DB_INIT = $env:DB_INIT -or 'false'
$env:INIT_SQL = $env:INIT_SQL -or 'sql/populate_data.sql'

$PG_SUPERUSER = $env:PG_SUPERUSER -or 'postgres'
$PG_SUPERPASS = $env:PG_SUPERPASS -or ''
$PG_HOST = $env:PG_HOST -or 'localhost'
$PG_PORT = $env:PG_PORT -or '5432'

function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Warn($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Err($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

if ($CreateDb) {
    if (-not $PG_SUPERPASS) {
        Write-Err "Creating DB requires superuser password. Set environment variable PG_SUPERPASS or run with it set."
        exit 1
    }

    # check psql availability
    $psql = Get-Command psql -ErrorAction SilentlyContinue
    if (-not $psql) {
        Write-Err "psql not found in PATH. Install PostgreSQL client or add psql to PATH to use -CreateDb."
        exit 1
    }

    Write-Info "Checking if database '$DB_NAME' exists on $PG_HOST:$PG_PORT ..."
    $env:PGPASSWORD = $PG_SUPERPASS
    $exists = & psql -h $PG_HOST -p $PG_PORT -U $PG_SUPERUSER -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>$null
    $exists = $exists.Trim()
    if ($exists -eq '1') {
        Write-Info "Database '$DB_NAME' already exists. Skipping creation."
    } else {
        Write-Info "Database '$DB_NAME' not found. Creating DB and user using 'sql/create_user_and_db.sql' as superuser..."
        $createResult = & psql -h $PG_HOST -p $PG_PORT -U $PG_SUPERUSER -f "sql/create_user_and_db.sql"
        if ($LASTEXITCODE -ne 0) {
            Write-Err "psql returned non-zero exit code while creating DB/user. See output above."
            exit $LASTEXITCODE
        }
        Write-Info "DB and user created (or SQL executed)."
    }
    # cleanup PGPASSWORD for safety
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Info "Starting application with DB URL=$env:DB_URL and user=$env:DB_USERNAME"

# start via maven wrapper (recommended in dev)
if (Test-Path ".\mvnw.cmd") {
    & .\mvnw.cmd -DskipTests spring-boot:run
} elseif (Test-Path ".\target\RADAR-0.0.1-SNAPSHOT.jar") {
    & java -jar .\target\RADAR-0.0.1-SNAPSHOT.jar
} else {
    Write-Err "No mvnw.cmd or target JAR found. Run '.\mvnw.cmd -DskipTests package' first or build the project." 
    exit 1
}
