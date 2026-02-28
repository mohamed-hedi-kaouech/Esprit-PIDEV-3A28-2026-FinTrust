package org.example.Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AIServiceTest {

    @Test
    public void emptyTextGivesDefaults() {
        AIService.AIResult res = AIService.process("");
        assertNotNull(res.getRephrased());
        assertNotNull(res.getAnalysis());
        assertTrue(res.getAnalysis().contains("no content") || res.getAnalysis().contains("words=0"));
    }

    @Test
    public void simpleTextAnalysis() {
        String text = "Bonjour monde\nBonjour";
        AIService.AIResult res = AIService.process(text);
        assertTrue(res.getAnalysis().contains("words=2") || res.getAnalysis().contains("words=3"));
        assertTrue(res.getRephrased().startsWith("[rephrased]") || res.getRephrased().length() > 0);
    }
}