@echo off
REM Gmail Setup and PIDEV Launch Script
REM Run this script to configure Gmail and launch the application

PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0setup_gmail.ps1'"
pause
