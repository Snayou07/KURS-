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

    public Ad(String title, String description, double price, String categoryId, String sellerId, List<String> imagePaths) {
        this.adId = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        // Создаем защитную копию списка
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    public Ad(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    public String getAdId() { return adId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategoryId() { return categoryId; }
    public String getSellerId() { return sellerId; }

    // Возвращаем неизменяемую копию списка
    public List<String> getImagePaths() {
        return Collections.unmodifiableList(imagePaths);
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    // Создаем защитную копию списка при установке
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
    public int hashCode() { return Objects.hash(adId); }

    @Override
    public String toString() {
        return "Ad{" +
                "adId='" + adId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' + // Добавлено для полноты, можно убрать если не нужно
                ", price=" + price +
                ", categoryId='" + categoryId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", imagePathsCount=" + (imagePaths != null ? imagePaths.size() : 0) +
                '}';
    }

    // Удален дублирующийся вложенный класс Ad
}