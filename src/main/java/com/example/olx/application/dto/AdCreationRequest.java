// src/main/java/com/example/olx/application/dto/AdCreationRequest.java
package com.example.olx.application.dto;

import java.util.List;

public class AdCreationRequest {
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String sellerId;
    private List<String> imagePaths;
    private boolean premium;
    private boolean urgent;

    // Конструктори
    public AdCreationRequest() {}

    public AdCreationRequest(String title, String description, double price, String categoryId, String sellerId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.premium = false;
        this.urgent = false;
    }

    public AdCreationRequest(String title, String description, double price, String categoryId,
                             String sellerId, List<String> imagePaths, boolean premium, boolean urgent) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.imagePaths = imagePaths;
        this.premium = premium;
        this.urgent = urgent;
    }

    // Геттери та сеттери
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    @Override
    public String toString() {
        return "AdCreationRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", categoryId='" + categoryId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", imagePaths=" + imagePaths +
                ", premium=" + premium +
                ", urgent=" + urgent +
                '}';
    }
}