package org.example.moderation.service;

import java.util.List;

public record ModerationResult(boolean allowed, String reason, String code, List<String> categories) {
}
