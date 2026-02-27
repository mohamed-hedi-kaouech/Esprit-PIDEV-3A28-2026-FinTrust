package org.example.Service.WalletService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class CurrencyService {

    private static final String API_KEY = "558b8d45997e0dab901950db"; // À obtenir gratuitement
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ✅ Obtenir le taux entre deux devises
    public double getTaux(String de, String vers) throws IOException {
        String url = BASE_URL + API_KEY + "/pair/" + de + "/" + vers;

        System.out.println("🔍 Appel API: " + url);  // ← AJOUTE ÇA

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("📡 Réponse: " + response.code());  // ← AJOUTE ÇA

            if (!response.isSuccessful()) {
                String body = response.body().string();
                System.out.println("❌ Corps de l'erreur: " + body);  // ← AJOUTE ÇA
                throw new IOException("Erreur API: " + response.code());
            }

            String jsonString = response.body().string();
            System.out.println("✅ Réponse JSON: " + jsonString);  // ← AJOUTE ÇA

            JsonNode json = mapper.readTree(jsonString);
            return json.get("conversion_rate").asDouble();
        }
    }

    // ✅ Convertir un montant
    public double convertir(String de, String vers, double montant) throws IOException {
        double taux = getTaux(de, vers);
        return montant * taux;
    }

    // ✅ Obtenir toutes les devises supportées
    public String[] getDevisesSupportees() {
        return new String[]{"TND", "EUR", "USD", "GBP", "CHF", "CAD", "JPY"};
    }
}