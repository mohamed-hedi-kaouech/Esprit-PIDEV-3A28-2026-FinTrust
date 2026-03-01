package org.example.Utils;

import okhttp3.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Email sending via external webhook service using OkHttp.
 *
 * Configure `WEBHOOK_URL` to point to a service that accepts multipart/form-data
 * with fields: `to`, `subject`, `body` and a file part named `file`.
 */
public class EmailService {

    public static String WEBHOOK_URL = "http://localhost:5680/api/send-email";
    public static String DEFAULT_TO = "recipient@example.com";

    public static void sendReportViaWebhook(Path file, String to, String subject, String body) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/pdf");
        RequestBody fileBody = RequestBody.create(file.toFile(), mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("to", to)
                .addFormDataPart("subject", subject)
                .addFormDataPart("body", body)
                .addFormDataPart("file", file.getFileName().toString(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(WEBHOOK_URL)
                .post(requestBody)
                .build();

        try (Response resp = client.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("Webhook error: " + resp.code() + " - " + resp.body().string());
            }
        }
    }
}
