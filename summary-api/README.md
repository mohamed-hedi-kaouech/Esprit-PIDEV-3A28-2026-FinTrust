# Summary API (IA)

Endpoint:

- `POST /api/summary/publication`

Port:

- `8082`

## Run

```powershell
cd summary-api
mvn spring-boot:run
```

Optional for true AI summary:

```powershell
setx OPENAI_API_KEY "YOUR_KEY"
```

Without key, the API returns a local fallback summary (still same response format).

## Request example

```json
{
  "publicationId": 5,
  "title": "Nouvelle offre épargne",
  "comments": [
    "Service utile et rapide",
    "L'application est stable mais parfois lente",
    "Bon accompagnement client"
  ]
}
```

## Response example

```json
{
  "summary": "Globalement, les clients apprécient...",
  "sentiment": "NEUTRAL",
  "ratingLabel": "Neutre"
}
```
