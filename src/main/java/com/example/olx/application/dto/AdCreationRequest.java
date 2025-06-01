// com/example/olx/application/dto/AdCreationRequest.java
package com.example.olx.application.dto;
import java.util.List; // Додайте цей імпорт
import java.util.ArrayList; // Додайте цей імпорт

public class AdCreationRequest {
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String sellerId;
    private List<String> imagePaths;// ID користувача, який створює оголошення
    private String userId;


    public AdCreationRequest(String title, String description, double price, String categoryId, String sellerId, List<String> imagePaths) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategoryId() { return categoryId; }
    public String getSellerId() { return sellerId; }
    // Конструктор для сумісності (якщо фото не передаються)
    public AdCreationRequest(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }
    public List<String> getImagePaths() {
        return imagePaths;
    }

    public String getUserId() {
        return userId;
    }
}