# Moderation API (AI)

Endpoint:

- `POST /api/moderation/check`

Port:

- `8080`

## Pre-requisite

Set OpenAI API key in environment variable:

```powershell
setx OPENAI_API_KEY "YOUR_KEY_HERE"
```

Restart terminal/IDE after `setx`.

## Run

```powershell
cd moderation-api
mvn spring-boot:run
```

## Request example

```bash
curl -X POST "http://localhost:8080/api/moderation/check" ^
  -H "Content-Type: application/json" ^
  -d "{\"text\":\"retourne dans ton pays\"}"
```

## Response example

```json
{
  "allowed": false,
  "reason": "Contenu inapproprie detecte",
  "code": "FLAGGED",
  "categories": ["hate"]
}
```

The response keeps `allowed` to stay compatible with your JavaFX client.
