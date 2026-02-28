# Script to run PIDEV with local SMTP4Dev server

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PIDEV Email Alert Test Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if smtp4dev is installed
Write-Host "`n[1/3] Checking smtp4dev..." -ForegroundColor Yellow
try {
    $smtp4dev = Get-Command smtp4dev -ErrorAction SilentlyContinue
    if ($smtp4dev) {
        Write-Host "✓ smtp4dev found" -ForegroundColor Green
    } else {
        Write-Host "✗ smtp4dev not found. Install with: choco install smtp4dev" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Error checking smtp4dev: $_" -ForegroundColor Red
    exit 1
}

# Start smtp4dev in background
Write-Host "`n[2/3] Starting smtp4dev server..." -ForegroundColor Yellow
Start-Process smtp4dev -NoNewWindow -PassThru | Out-Null
Start-Sleep -Seconds 3
Write-Host "✓ smtp4dev started (http://localhost:3000)" -ForegroundColor Green

# Configure environment variables and run app
Write-Host "`n[3/3] Configuring environment and launching app..." -ForegroundColor Yellow
$env:SMTP_HOST = "localhost"
$env:SMTP_PORT = "25"
$env:SMTP_USER = ""
$env:SMTP_PASS = ""
$env:SMTP_FROM = "alerts@pidev.local"
$env:ALERT_EMAIL_TO = "admin@pidev.local"
$env:SMTP_USE_STARTTLS = "false"

Write-Host "`nEnvironment variables configured:" -ForegroundColor Cyan
Write-Host "  SMTP_HOST = localhost"
Write-Host "  SMTP_PORT = 25"
Write-Host "  SMTP_FROM = alerts@pidev.local"
Write-Host "  ALERT_EMAIL_TO = admin@pidev.local"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Launching PIDEV..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nTest flow:" -ForegroundColor Green
Write-Host "1. Add a category (e.g., 'Test') with seuilAlerte = 50 DT"
Write-Host "2. Add items that total > 50 DT"
Write-Host "3. Email sent to smtp4dev when threshold exceeded"
Write-Host "4. View emails at http://localhost:3000"
Write-Host ""

mvn javafx:run
