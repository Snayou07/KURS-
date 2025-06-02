// com/example/olx/application/dto/AdCreationRequest.java
package com.example.olx.application.dto;
import java.util.List;
import java.util.ArrayList;

public class AdCreationRequest {
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String sellerId;
    private List<String> imagePaths;
    private String userId; // ID користувача, який створює оголошення

    // Основний конструктор з усіма параметрами
    public AdCreationRequest(String title, String description, double price, String categoryId, String sellerId, List<String> imagePaths) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.userId = sellerId; // Встановлюємо userId рівним sellerId за замовчуванням
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    // Конструктор для сумісності (якщо фото не передаються)
    public AdCreationRequest(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    // Конструктор з userId
    public AdCreationRequest(String title, String description, double price, String categoryId, String sellerId, String userId, List<String> imagePaths) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.userId = userId;
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    // Геттери
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public String getUserId() {
        return userId;
    }

    // Сеттери для можливості модифікації
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }
}