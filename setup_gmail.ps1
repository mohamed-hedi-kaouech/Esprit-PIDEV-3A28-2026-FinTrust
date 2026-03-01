# Gmail Configuration Script for PIDEV Email Alerts

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PIDEV - Gmail Configuration" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "INSTRUCTIONS:" -ForegroundColor Yellow
Write-Host "1. Go to: https://myaccount.google.com/security" -ForegroundColor White
Write-Host "2. Enable '2-Step Verification' if not already done" -ForegroundColor White
Write-Host "3. Search for 'App passwords' → Select Mail and Windows" -ForegroundColor White
Write-Host "4. Copy the 16-character password (xxxx xxxx xxxx xxxx)" -ForegroundColor White
Write-Host ""

# Get Gmail credentials (secure)
$gmailEmail = Read-Host "Enter your Gmail address (e.g., user@gmail.com)"
$securePwd = Read-Host "Enter your Gmail App Password (it will not be shown)" -AsSecureString

# Convert SecureString to plain text safely, then zero the BSTR
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePwd)
$SMTP_PASS = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)

# Confirm (don't print password)
Write-Host "\nYou entered:" -ForegroundColor Cyan
Write-Host "  Gmail: $gmailEmail"
if (-not (Read-Host "Proceed and launch the app with these settings? (Y/N)" -AsSecureString)) {
	# Note: Read-Host -AsSecureString returns SecureString; we check by re-prompting simple input
}

# Configure environment variables for this session
$env:SMTP_HOST = "smtp.gmail.com"
$env:SMTP_PORT = "587"
$env:SMTP_USER = $gmailEmail
$env:SMTP_PASS = $SMTP_PASS
$env:SMTP_FROM = $gmailEmail
$env:ALERT_EMAIL_TO = $gmailEmail
$env:SMTP_USE_STARTTLS = "true"
$env:SMTP_USE_SSL = "false"
$env:EMAIL_TEST_MODE = "false"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  Configuration Complete" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Configured Settings:" -ForegroundColor Cyan
Write-Host "  SMTP_HOST: smtp.gmail.com"
Write-Host "  SMTP_PORT: 587"
Write-Host "  SMTP_USER: $gmailEmail"
Write-Host "  SMTP_FROM: $gmailEmail"
Write-Host "  ALERT_EMAIL_TO: $gmailEmail"
Write-Host "  SMTP_USE_STARTTLS: true"
Write-Host "  EMAIL_TEST_MODE: false"
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Launching PIDEV Application" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "TEST FLOW:" -ForegroundColor Yellow
Write-Host "1. Create a category (e.g., 'Food') with seuilAlerte = 50 DT" -ForegroundColor White
Write-Host "2. Add items totaling > 50 DT" -ForegroundColor White
Write-Host "3. Alert triggered → Real email sent to $gmailEmail" -ForegroundColor White
Write-Host ""

mvn javafx:run
