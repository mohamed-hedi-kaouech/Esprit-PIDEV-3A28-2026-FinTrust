package org.example.Utils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ApiServerTest {
    private static ApiServer server;
    private static OkHttpClient client;

    @BeforeAll
    public static void setup() throws IOException {
        server = new ApiServer();
        server.start();
        client = new OkHttpClient();
    }

    @AfterAll
    public static void tearDown() {
        if (server != null) server.stop();
    }

    @Test
    public void generateReportEndpoint() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:5680/api/generate-report")
                .post(RequestBody.create(new byte[0]))
                .build();
        try (Response resp = client.newCall(request).execute()) {
            assertTrue(resp.isSuccessful());
            String body = resp.body().string();
            assertTrue(body.startsWith("OK:"));
        }
    }

    @Test
    public void analyzeReportEndpoint() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:5680/api/analyze-report")
                .post(RequestBody.create(new byte[0]))
                .build();
        try (Response resp = client.newCall(request).execute()) {
            assertTrue(resp.isSuccessful());
            String body = resp.body().string();
            assertTrue(body.contains("\"pdfPath\""));
            assertTrue(body.contains("\"ocrText\""));
            assertTrue(body.contains("\"aiRephrased\""));
            assertTrue(body.contains("\"aiAnalysis\""));
        }
    }
}