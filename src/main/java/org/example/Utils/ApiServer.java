package org.example.Utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.Service.BudgetService.BudgetService;
import org.example.Service.BudgetService.ItemService;
import org.example.Model.Budget.Categorie;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Minimal local HTTP API server exposing an endpoint to generate the PDF report.
 * Start this server from your application (e.g., in MainFX) if you want a local API.
 */
public class ApiServer {
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(5680), 0);
        server.createContext("/api/generate-report", new GenerateReportHandler());
        server.createContext("/api/analyze-report", new AnalyzeReportHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("[ApiServer] listening on http://localhost:5680/api/generate-report");
        System.out.println("[ApiServer] listening on http://localhost:5680/api/analyze-report (generates PDF + OCR + AI)");
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    static class GenerateReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String r = "Only POST is supported";
                exchange.sendResponseHeaders(405, r.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(r.getBytes()); }
                return;
            }

            try {
                BudgetService bs = new BudgetService();
                ItemService is = new ItemService();
                List<Categorie> cats = bs.ReadAll();
                java.nio.file.Path out = org.example.Utils.PdfReportService.createCategoryReport(cats, is);
                String resp = "OK: " + out.toAbsolutePath().toString();
                exchange.sendResponseHeaders(200, resp.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(resp.getBytes(StandardCharsets.UTF_8)); }
            } catch (Exception e) {
                e.printStackTrace();
                String err = "ERROR: " + e.getMessage();
                exchange.sendResponseHeaders(500, err.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err.getBytes()); }
            }
        }
    }

    static class AnalyzeReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String r = "Only POST is supported";
                exchange.sendResponseHeaders(405, r.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(r.getBytes()); }
                return;
            }

            try {
                BudgetService bs = new BudgetService();
                ItemService is = new ItemService();
                List<Categorie> cats = bs.ReadAll();
                PdfReportService.ReportResult result = PdfReportService.createAndAnalyzeCategoryReport(cats, is);

                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"pdfPath\":\"").append(result.getPdfPath().toAbsolutePath()).append("\",");
                json.append("\"ocrText\":\"").append(escape(result.getOcrText())).append("\",");
                json.append("\"aiRephrased\":\"").append(escape(result.getAiResult().getRephrased())).append("\",");
                json.append("\"aiAnalysis\":\"").append(escape(result.getAiResult().getAnalysis())).append("\"");
                json.append("}");

                byte[] respBytes = json.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, respBytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(respBytes); }
            } catch (Exception e) {
                e.printStackTrace();
                String err = "ERROR: " + e.getMessage();
                exchange.sendResponseHeaders(500, err.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err.getBytes()); }
            }
        }

        private String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }
    }
}
