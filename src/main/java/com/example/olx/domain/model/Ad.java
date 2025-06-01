package com.example.olx.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Ad implements Serializable, Cloneable {
    private static final long serialVersionUID = 4L;

    private String adId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String sellerId;
    private List<String> imagePaths;
    private String status;
    private AdState currentState;
    private boolean premium;
    private boolean urgent;
    private AdState state;
    private Object id;

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
        this.currentState = new DraftAdState(); // Початковий стан - чернетка
        this.status = currentState.getStatusName();

    }

    // Конструктор без imagePaths (для зворотної сумісності)
    public Ad(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    // Порожній конструктор (може знадобитися для деяких фреймворків)
    public Ad() {
        this.adId = UUID.randomUUID().toString();
        this.imagePaths = new ArrayList<>();
        this.currentState = new DraftAdState();
        this.status = currentState.getStatusName();

    }

    // Методи для роботи зі станами
    public void publishAd() {
        currentState.publish(this);
        updateStatus();
    }

    public void archiveAd() {
        currentState.archive(this);
        updateStatus();
    }

    public void markAsSold() {
        currentState.markAsSold(this);
        updateStatus();
    }

    public void setCurrentState(AdState state) {
        this.currentState = state;
        updateStatus();
    }

    public AdState getCurrentState() {
        return currentState;
    }

    private void updateStatus() {
        if (currentState != null) {
            this.status = currentState.getStatusName();
        }
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

    public String getStatus() {
        return status;
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

    public void setStatus(String status) {
        this.status = status;
    }

    // Створюємо захисну копію списку при встановленні
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    // Імплементація клонування
    @Override
    public Ad clone() throws CloneNotSupportedException {
        Ad cloned = (Ad) super.clone();
        cloned.imagePaths = new ArrayList<>(this.imagePaths);
        // Створюємо новий стан на основі поточного
        cloned.currentState = createStateFromStatus(this.status);
        return cloned;
    }

    private AdState createStateFromStatus(String status) {
        switch (status) {
            case "Чернетка":
                return new DraftAdState();
            case "Активне":
                return new ActiveAdState();
            case "Архівоване":
                return new ArchivedAdState();
            case "Продано":
                return new SoldAdState();
            case "На модерації":
                return new ModerationAdState();
            default:
                return new DraftAdState();
        }
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
                ", status='" + status + '\'' +
                ", imagePathsCount=" + (imagePaths != null ? imagePaths.size() : 0) +
                '}';
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

    public boolean hasDiscount() {
        return false;
    }

    public void setState(AdState state) {
        this.state = state;
    }

    public Object getId() {
        return id;
    }
}