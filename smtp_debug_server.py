#!/usr/bin/env python3
"""
Simple SMTP server for testing email alerts.
Prints all received emails to console.
"""
import smtpd
import asyncore
import sys

class DebugSMTPServer(smtpd.SMTPServer):
    def process_message(self, peer, mailfrom, rcpttos, data):
        print()
        print("=" * 60)
        print(f"✉️  EMAIL RECEIVED")
        print("=" * 60)
        print(f"From: {peer[0]}:{peer[1]}")
        print(f"Sender: {mailfrom}")
        print(f"Recipients: {', '.join(rcpttos)}")
        print("-" * 60)
        print(data.decode('utf-8'))
        print("=" * 60)
        print()

if __name__ == '__main__':
    server = DebugSMTPServer(('localhost', 25), None)
    print()
    print("=" * 60)
    print("📧 SMTP Debug Server Started")
    print("=" * 60)
    print("Host: localhost")
    print("Port: 25")
    print("Listening for emails...")
    print("=" * 60)
    print()
    asyncore.loop()
