package org.example.summary.api;

import jakarta.validation.Valid;
import org.example.summary.service.AiSummaryService;
import org.example.summary.service.SummaryResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
@CrossOrigin(origins = "*")
public class SummaryController {

    private final AiSummaryService aiSummaryService;

    public SummaryController(AiSummaryService aiSummaryService) {
        this.aiSummaryService = aiSummaryService;
    }

    @PostMapping("/publication")
    public ResponseEntity<SummaryResponse> summarize(@Valid @RequestBody SummaryRequest request) {
        SummaryResult result = aiSummaryService.summarize(
                request.getPublicationId(),
                request.getTitle(),
                request.getComments()
        );
        return ResponseEntity.ok(new SummaryResponse(
                result.summary(),
                result.sentiment(),
                result.ratingLabel()
        ));
    }
}
