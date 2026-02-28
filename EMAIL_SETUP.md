# PIDEV Email Alerts - Configuration Guide

## Quick Start (Gmail)

### Option 1: Automated Setup (Recommended)

1. **Double-click** `setup_gmail.bat` in the project folder
2. Enter your Gmail address
3. Enter your Gmail App Password
4. App launches automatically with real email sending enabled

### Option 2: Manual Configuration

If the script doesn't work, configure manually:

```powershell
$env:SMTP_HOST='smtp.gmail.com'
$env:SMTP_PORT='587'
$env:SMTP_USER='your.email@gmail.com'
$env:SMTP_PASS='your-app-password'
$env:SMTP_FROM='your.email@gmail.com'
$env:ALERT_EMAIL_TO='your.email@gmail.com'
$env:SMTP_USE_STARTTLS='true'
$env:EMAIL_TEST_MODE='false'
mvn javafx:run
```

---

## Getting Gmail App Password

Gmail requires an "App Password" for SMTP (not your regular password):

1. Go to: **https://myaccount.google.com/security**
2. Enable **"2-Step Verification"** if not already enabled
3. Search for **"App passwords"** in the settings
4. Select **Mail** and **Windows**
5. Google will generate a 16-character password (format: `xxxx xxxx xxxx xxxx`)
6. Copy this password and use it in the setup script

---

## Testing the Email Feature

### Test Flow

1. Launch the app with the setup script
2. Go to **Budget → Categories**
3. Add a new category (e.g., "Food") with:
   - Name: `Food`
   - seuilAlerte: `50` DT
4. Go to **Budget → Items**
5. Select the `Food` category
6. Add items that total **more than 50 DT** (e.g., 3 items × 20 DT = 60 DT)
7. **Automatic email sent!** Check your Gmail inbox

### What You'll Receive

When the threshold is exceeded, you'll get an email like:

```
From: alerts@pidev.local
To: your.email@gmail.com
Subject: Alerte Budget: Food

⚠️ Alerte de dépassement de budget

Catégorie: Food
Message: La catégorie 'Food' a atteint le seuil...
Seuil: 50.00 DT
Date: ...
```

---

## Environment Variables Reference

| Variable | Value | Example |
|----------|-------|---------|
| SMTP_HOST | SMTP server | `smtp.gmail.com` |
| SMTP_PORT | Port number | `587` |
| SMTP_USER | Email login | `user@gmail.com` |
| SMTP_PASS | App password | `xxxx xxxx xxxx xxxx` |
| SMTP_FROM | Sender address | `user@gmail.com` |
| ALERT_EMAIL_TO | Recipient | `user@gmail.com` |
| SMTP_USE_STARTTLS | true/false | `true` |
| EMAIL_TEST_MODE | true/false | `false` (for real emails) |

---

## Troubleshooting

### "Authentication failed"
- Verify Gmail App Password is correct (16 chars, no spaces removed)
- Make sure 2-Step Verification is enabled on your Google account

### "Cannot connect to host"
- Check internet connection
- Verify SMTP_HOST and SMTP_PORT are correct

### Emails sent but not received
- Check Gmail **Spam/Junk** folder
- Check that recipient email is correct (`ALERT_EMAIL_TO`)

### Test Mode
To see emails in console without sending (for debugging):

```powershell
$env:EMAIL_TEST_MODE='true'
mvn javafx:run
```

---

## Other Email Providers

### Outlook/Office365

```powershell
$env:SMTP_HOST='smtp.office365.com'
$env:SMTP_PORT='587'
$env:SMTP_USER='your.email@outlook.com'
$env:SMTP_PASS='your-password'
$env:SMTP_FROM='your.email@outlook.com'
$env:ALERT_EMAIL_TO='your.email@outlook.com'
$env:SMTP_USE_STARTTLS='true'
$env:EMAIL_TEST_MODE='false'
mvn javafx:run
```

### Any SMTP Server

Adjust the variables to match your provider's SMTP settings.

---

Created: Feb 28, 2026
