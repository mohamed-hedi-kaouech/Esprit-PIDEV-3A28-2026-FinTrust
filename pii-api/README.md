# PII API (Detection donnees sensibles)

Endpoint:

- `POST /api/pii/check`

Port:

- `8081` (configure dans `application.properties`)

## Run

```powershell
cd pii-api
mvn spring-boot:run
```

## Example request

```bash
curl -X POST "http://localhost:8081/api/pii/check" \
  -H "Content-Type: application/json" \
  -d "{\"text\":\"mon rib est TN59 1000 6035 1835 9847 8831\"}"
```

## Example response

```json
{
  "allowed": false,
  "detected": ["IBAN"],
  "reason": "Donnee sensible detectee"
}
```

## Detected categories

- `IBAN`
- `RIB`
- `CARD_NUMBER`
- `CIN_PASSPORT`
- `PHONE`
- `EMAIL`
