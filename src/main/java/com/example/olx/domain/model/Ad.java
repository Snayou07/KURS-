// src/main/java/com/example/olx/domain/model/Ad.java
package com.example.olx.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private AdState currentState; // Залишаємо тільки це поле
    private boolean premium;
    private boolean urgent;
    private String id;
    private Object state;
    private LocalDateTime createdAt;
    private boolean hasDelivery;
    private boolean hasWarranty;
    private LocalDateTime updatedAt;
    private boolean hasDiscount;


    // Основний конструктор з усіма параметрами
    public Ad(String title, String description, double price, String categoryId, String sellerId, List<String> imagePaths) {
        this.adId = UUID.randomUUID().toString();
        this.id = this.adId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
        this.currentState = new DraftAdState();
        this.status = currentState.getStatusName();
    }
    public boolean hasDelivery() {
        return this.hasDelivery;
    }

    /**
     * Встановлює опцію доставки
     */
    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
        if (this.updatedAt != null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Перевіряє чи має оголошення гарантію
     */
    public boolean hasWarranty() {
        return this.hasWarranty;
    }

    /**
     * Встановлює опцію гарантії
     */
    public void setHasWarranty(boolean hasWarranty) {
        this.hasWarranty = hasWarranty;
        if (this.updatedAt != null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Також додайте інші методи, якщо потрібно:

  
   

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
        if (this.updatedAt != null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    // Конструктор без imagePaths
    public Ad(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    // Порожній конструктор
    public Ad() {
        this.adId = UUID.randomUUID().toString();
        this.id = this.adId;
        this.imagePaths = new ArrayList<>();
        this.currentState = new DraftAdState();
        this.status = currentState.getStatusName();
    }

    // Методи для роботи зі станами
    public void publishAd() {
        if (currentState != null) {
            currentState.publish(this);
            updateStatus();
        }
    }

    public void archiveAd() {
        if (currentState != null) {
            currentState.archive(this);
            updateStatus();
        }
    }

    public void markAsSold() {
        if (currentState != null) {
            currentState.markAsSold(this);
            updateStatus();
        }
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
    public static class DateUtils {

        /**
         * Конвертує LocalDate в строку
         */
        public static String toLocalDate(LocalDate date) {
            if (date == null) {
                return "";
            }
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        /**
         * Конвертує строку в LocalDate
         */
        public static LocalDate fromString(String dateString) {
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (Exception e) {
                return null;
            }
        }
    }

// АБО додайте цей метод безпосередньо в клас, де він використовується:

    /**
     * Конвертує LocalDate в строкове представлення
     */
    public String toLocalDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    // Геттери та сеттери
    public String getAdId() { return adId; }
    public String getId() { return id != null ? id : adId; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSellerId() { return sellerId; }

    public List<String> getImagePaths() {
        return Collections.unmodifiableList(imagePaths);
    }
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public boolean hasDiscount() { return false; }

    // Імплементація клонування
    @Override
    public Ad clone() throws CloneNotSupportedException {
        Ad cloned = (Ad) super.clone();
        cloned.imagePaths = new ArrayList<>(this.imagePaths);
        cloned.currentState = createStateFromStatus(this.status);
        return cloned;
    }

    private AdState createStateFromStatus(String status) {
        if (status == null) return new DraftAdState();

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

    public Object getState() {
        return state;
    }

    public void setState(AdState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt; // LocalDateTime implements Comparable
    }
}