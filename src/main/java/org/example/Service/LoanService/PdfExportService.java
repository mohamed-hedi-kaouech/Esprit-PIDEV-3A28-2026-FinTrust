package org.example.Service.LoanService;

import org.example.Model.Loan.LoanClass.Repayment;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class PdfExportService {

    private final String apiKey = "O3Mv1AZcBxrDeZ6i_0Qm";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public byte[] generatePdfFromRepayments(List<Repayment> plan, int loanId) throws Exception {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set DOCRAPTOR_API_KEY in environment variables.");
        }

        String html = buildHtml(plan, loanId);

        return callExternalPdfApi(html);
    }

    private byte[] callExternalPdfApi(String html) throws Exception {

        String url = "https://api.docraptor.com/docs?user_credentials="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        String jsonBody = """
        {
          "type": "pdf",
          "document_content": %s
        }
        """.formatted(toJsonString(html));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<byte[]> response =
                http.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }

        throw new RuntimeException("PDF API Error: " + response.statusCode());
    }

    private String buildHtml(List<Repayment> plan, int loanId) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial; }
                h2 { margin-bottom: 10px; }
                table { width: 100%; border-collapse: collapse; }
                th, td { border: 1px solid #333; padding: 8px; font-size: 12px; }
                th { background: #f2f2f2; }
                .num { text-align: right; }
            </style>
        </head>
        <body>
        """);

        sb.append("<h2>Plan d'amortissement")
                .append("</h2>");

        sb.append("""
        <table>
            <tr>
                <th>Mois</th>
                <th>Solde début</th>
                <th>Mensualité</th>
                <th>Capital</th>
                <th>Intérêt</th>
                <th>Solde restant</th>
                <th>Statut</th>
            </tr>
        """);

        for (Repayment r : plan) {

            sb.append("<tr>")
                    .append("<td>").append(r.getMonth()).append("</td>")
                    .append("<td class='num'>").append(format(r.getStartingBalance())).append("</td>")
                    .append("<td class='num'>").append(format(r.getMonthlyPayment())).append("</td>")
                    .append("<td class='num'>").append(format(r.getCapitalPart())).append("</td>")
                    .append("<td class='num'>").append(format(r.getInterestPart())).append("</td>")
                    .append("<td class='num'>").append(format(r.getRemainingBalance())).append("</td>")
                    .append("<td>").append(r.getStatus()).append("</td>")
                    .append("</tr>");
        }

        sb.append("</table></body></html>");

        return sb.toString();
    }

    private String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String toJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}