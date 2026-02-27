package org.example.summary.api;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SummaryRequest {
    private int publicationId;
    @NotNull
    private String title;
    @NotNull
    private List<String> comments;

    public int getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(int publicationId) {
        this.publicationId = publicationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}
