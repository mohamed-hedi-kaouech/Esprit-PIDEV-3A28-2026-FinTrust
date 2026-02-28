@echo off
REM Script to test PIDEV email alerts in test mode
echo.
echo ========================================
echo  PIDEV Email Alert Test
echo ========================================
echo.
echo Mode: TEST (emails logged to console)
echo.
echo Test flow:
echo 1. Add a category with seuilAlerte = 50 DT
echo 2. Add items that total over 50 DT
echo 3. Alert created + email shown in console
echo.
echo ========================================
echo.

REM Email is in test mode by default
set EMAIL_TEST_MODE=true
set SMTP_HOST=localhost
set SMTP_PORT=25
set SMTP_FROM=alerts@pidev.local
set ALERT_EMAIL_TO=admin@pidev.local

mvn javafx:run
