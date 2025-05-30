package com.example.olx.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Ad implements Serializable {
    private static final long serialVersionUID = 4L;

    private String adId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String sellerId;
    private List<String> imagePaths;

    // Основний конструктор з усіма параметрами
    public Ad(String title, String description, double price, String categoryId, String sellerId, List<String> imagePaths) {
        this.adId = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        // Створюємо захисну копію списку
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    // Конструктор без imagePaths (для зворотної сумісності)
    public Ad(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    // Порожній конструктор (може знадобитися для деяких фреймворків)
    public Ad() {
        this.adId = UUID.randomUUID().toString();
        this.imagePaths = new ArrayList<>();
    }

    // Геттери
    public String getAdId() {
        return adId;
    }

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

    // Повертаємо незмінну копію списку
    public List<String> getImagePaths() {
        return Collections.unmodifiableList(imagePaths);
    }

    // Сеттери
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

    // Створюємо захисну копію списку при встановленні
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return Objects.equals(adId, ad.adId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adId);
    }

    @Override
    public String toString() {
        return "Ad{" +
                "adId='" + adId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", categoryId='" + categoryId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", imagePathsCount=" + (imagePaths != null ? imagePaths.size() : 0) +
                '}';
    }
}