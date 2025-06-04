// src/main/java/com/example/olx/domain/model/Ad.java
package com.example.olx.domain.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Ad implements Serializable, Cloneable {
    private static final long serialVersionUID = 4L;

    // Основные поля
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
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Дополнительные поля для функциональности
    private boolean hasDelivery;
    private boolean freeDelivery;
    private double deliveryCost;
    private String deliveryInfo;

    private boolean hasWarranty;
    private int warrantyMonths;
    private String warrantyType;

    private boolean hasDiscount;
    private double discountPercentage;
    private String discountReason;
    private String adState;

    // Основной конструктор с всеми параметрами
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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

    }
    // src/main/java/com/example/olx/domain/model/AdState.java


    // Конструктор без imagePaths
    public Ad(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    // Пустой конструктор
    public Ad() {
        this.adId = UUID.randomUUID().toString();
        this.id = this.adId;
        this.imagePaths = new ArrayList<>();
        this.currentState = new DraftAdState();
        this.status = currentState.getStatusName();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Методы для работы с состояниями
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
        this.updatedAt = LocalDateTime.now();
    }

    public AdState getCurrentState() {
        return currentState;
    }

    private void updateStatus() {
        if (currentState != null) {
            this.status = currentState.getStatusName();
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Методы для работы с доставкой
    public boolean hasDelivery() {
        return this.hasDelivery;
    }

    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isFreeDelivery() {
        return freeDelivery;
    }

    public void setFreeDelivery(boolean freeDelivery) {
        this.freeDelivery = freeDelivery;
        this.hasDelivery = true;
        this.updatedAt = LocalDateTime.now();
    }

    public double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
        this.hasDelivery = true;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(String deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
        this.updatedAt = LocalDateTime.now();
    }

    // Методы для работы с гарантией
    public boolean hasWarranty() {
        return this.hasWarranty;
    }

    public void setHasWarranty(boolean hasWarranty) {
        this.hasWarranty = hasWarranty;
        this.updatedAt = LocalDateTime.now();
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
        this.hasWarranty = warrantyMonths > 0;
        this.updatedAt = LocalDateTime.now();
    }

    public String getWarrantyType() {
        return warrantyType;
    }

    public void setWarrantyType(String warrantyType) {
        this.warrantyType = warrantyType;
        this.updatedAt = LocalDateTime.now();
    }

    // Методы для работы со скидками
    public boolean hasDiscount() {
        return hasDiscount;
    }

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
        this.updatedAt = LocalDateTime.now();
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
        this.hasDiscount = discountPercentage > 0;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAdState() {
        return adState;
    }

    // Utility-класс для работы с датами (внутренний)
    public static class DateUtils {
        public static String formatDate(LocalDateTime dateTime) {
            if (dateTime == null) {
                return "";
            }
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        public static String formatDateOnly(LocalDateTime dateTime) {
            if (dateTime == null) {
                return "";
            }
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
    }

    // Геттеры и сеттеры
    public String getAdId() {
        return adId;
    }

    public String getId() {
        return id != null ? id : adId;
    }

    public void setId(String id) {
        this.id = id;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getImagePaths() {
        return Collections.unmodifiableList(imagePaths);
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Реализация клонирования
    @Override
    public Ad clone() throws CloneNotSupportedException {
        Ad cloned = (Ad) super.clone();
        cloned.imagePaths = new ArrayList<>(this.imagePaths);
        cloned.currentState = createStateFromStatus(this.status);
        cloned.createdAt = this.createdAt;
        cloned.updatedAt = LocalDateTime.now(); // Новое время для клона
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

    // Методы для совместимости с существующим кодом
    public Object getState() {
        return this.currentState;
    }

    public void setState(AdState state) {
        this.currentState = state;
        updateStatus();
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
                ", createdAt=" + DateUtils.formatDate(createdAt) +
                ", updatedAt=" + DateUtils.formatDate(updatedAt) +
                '}';
    }
}