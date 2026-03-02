package org.example.moderation.api;

import jakarta.validation.Valid;
import org.example.moderation.service.AiModerationService;
import org.example.moderation.service.ModerationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderation")
@CrossOrigin(origins = "*")
public class ModerationController {

    private final AiModerationService aiModerationService;

    public ModerationController(AiModerationService aiModerationService) {
        this.aiModerationService = aiModerationService;
    }

    @PostMapping("/check")
    public ResponseEntity<ModerationCheckResponse> check(@Valid @RequestBody ModerationCheckRequest request) {
        ModerationResult result = aiModerationService.check(request.getText());
        return ResponseEntity.ok(new ModerationCheckResponse(
                result.allowed(),
                result.reason(),
                result.code(),
                result.categories()
        ));
    }
}
