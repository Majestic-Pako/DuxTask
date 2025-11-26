package com.example.duxtask;

import java.util.List;

public class Task {
    private String documentId;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private List<String> state;
    private String userId;

    public Task() { }

    public Task(String title, String description, String category,
                String subcategory, List<String> state, String userId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.subcategory = subcategory;
        this.state = state;
        this.userId = userId;
    }

    public String getDocumentId() {return documentId;}
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public List<String> getState() { return state; }
    public String getUserId() { return userId; }

    public void setDocumentId(String documentId) {this.documentId = documentId;}
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public void setState(List<String> state) { this.state = state; }
    public void setUserId(String userId) { this.userId = userId; }
}
