# 💳 Fintrust

## 📌 Overview
**Fintrust** is a full-stack desktop application developed as part of the **PIDEV – 3rd Year Engineering Program (3A)** at **Esprit School of Engineering** during the academic year **2025–2026**.

The application allows teams to manage tasks, track progress, and collaborate efficiently through a structured and user-friendly system.

---

## 🚀 Features

- 👥 User management (authentication & roles)
- 📋 Task creation and assignment
- 📊 Progress tracking
- 📧 Workflow automation integration (via n8n)
- 🗄️ MySQL database persistence
- 🔐 Secure data handling
- 🖥️ Interactive JavaFX desktop interface

---

## 🛠️ Tech Stack

### Frontend
- JavaFX (FXML)

### Backend
- Java
- MVC Architecture

### Database
- MySQL

### Automation
- n8n

### DevOps
- Docker

---

## 🏗️ Architecture

The project follows the **MVC (Model-View-Controller)** pattern:

- **Model** → Manages data and business logic
- **View** → JavaFX (FXML) user interface
- **Controller** → Handles interaction between Model and View

This architecture ensures maintainability, scalability, and clean code organization.

---

## 📂 Project Structure
│── src/
│ ├── Model/
| ├── Interfaces/
│ ├── View/
│ ├── Controller/
│ └── Utils/

---

## ⚙️ Getting Started

### Prerequisites

- Java JDK 17+
- MySQL Server
- Docker (optional)
- n8n (optional)
- IntelliJ IDEA (recommended)

---

### Installation

1. Clone the repository:

```bash
git clone https://github.com/mohamed-hedi-kaouech/Esprit-PIDEV-3A28-2026-FinTrust.git