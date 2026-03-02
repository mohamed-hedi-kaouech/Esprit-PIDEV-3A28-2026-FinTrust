package org.example.pii.api;

import jakarta.validation.Valid;
import org.example.pii.service.PiiDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pii")
@CrossOrigin(origins = "*")
public class PiiController {

    private final PiiDetectionService piiDetectionService;

    public PiiController(PiiDetectionService piiDetectionService) {
        this.piiDetectionService = piiDetectionService;
    }

    @PostMapping("/check")
    public ResponseEntity<PiiCheckResponse> check(@Valid @RequestBody PiiCheckRequest request) {
        List<String> detected = piiDetectionService.detect(request.getText());
        boolean allowed = detected.isEmpty();
        String reason = allowed ? "OK" : "Donnee sensible detectee";
        return ResponseEntity.ok(new PiiCheckResponse(allowed, detected, reason));
    }
}
