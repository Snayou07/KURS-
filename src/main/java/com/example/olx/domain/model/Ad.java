package com.example.olx.domain.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Ad implements Serializable, Cloneable {
    private static final long serialVersionUID = 5L; // Оновлено serialVersionUID

    private String adId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private String sellerId;
    private List<String> imagePaths;
    private String status;
    private AdState currentState;
    private LocalDateTime createdAt;
    private Object id; // Зазвичай це той же adId або може бути іншим ідентифікатором з БД

    // Поля для декораторів
    private boolean premium;
    private boolean urgent;

    // Поля для знижки
    private boolean hasDiscount; // Прапорець наявності знижки
    private double discountPercentage;
    private String discountReason;

    // Поля для гарантії (прапорець hasWarranty вже існував)
    private boolean hasWarranty; // Прапорець наявності гарантії
    private int warrantyMonths;
    private String warrantyType;

    // Поля для доставки (прапорець hasDelivery вже існував)
    private boolean hasDelivery; // Прапорець наявності опції доставки
    private boolean freeDelivery; // Чи є доставка безкоштовною
    private double deliveryCost; // Вартість доставки, якщо не безкоштовна
    private String deliveryDetails; // Додаткова інформація про доставку


    // Основний конструктор з усіма основними параметрами
    public Ad(String title, String description, double price, String categoryId, String sellerId, List<String> imagePaths) {
        this.adId = UUID.randomUUID().toString();
        this.id = this.adId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
        this.currentState = new DraftAdState(); // Початковий стан - чернетка
        this.status = currentState.getStatusName();
        this.createdAt = LocalDateTime.now(); // Встановлюємо час створення

        // Ініціалізація значень за замовчуванням для нових полів
        this.premium = false;
        this.urgent = false;
        this.hasDiscount = false;
        this.discountPercentage = 0.0;
        this.discountReason = "";
        this.hasWarranty = false;
        this.warrantyMonths = 0;
        this.warrantyType = "";
        this.hasDelivery = false;
        this.freeDelivery = false;
        this.deliveryCost = 0.0;
        this.deliveryDetails = "";
    }

    // Конструктор без imagePaths
    public Ad(String title, String description, double price, String categoryId, String sellerId) {
        this(title, description, price, categoryId, sellerId, new ArrayList<>());
    }

    // Порожній конструктор
    public Ad() {
        this(null, null, 0.0, null, null, new ArrayList<>());
        // Для порожнього конструктора можна залишити adId і id, що генеруються в основному
        // або генерувати їх тут, якщо основний не викликається.
        // Оскільки this(...) викликає основний конструктор, там все ініціалізується.
    }

    // --- Методи для роботи зі станами ---
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

    // --- Геттери ---
    public String getAdId() { return adId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategoryId() { return categoryId; }
    public String getSellerId() { return sellerId; }
    public List<String> getImagePaths() { return Collections.unmodifiableList(imagePaths); }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Object getId() { return id != null ? id : adId; }


    // Геттери для полів декораторів
    public boolean isPremium() { return premium; }
    public boolean isUrgent() { return urgent; }

    public boolean hasDiscount() { return hasDiscount; } // Використовується для прапорця
    public double getDiscountPercentage() { return discountPercentage; }
    public String getDiscountReason() { return discountReason; }

    public boolean hasWarranty() { return hasWarranty; } // Використовується для прапорця
    public int getWarrantyMonths() { return warrantyMonths; }
    public String getWarrantyType() { return warrantyType; }

    public boolean hasDelivery() { return hasDelivery; } // Використовується для прапорця
    public boolean isFreeDelivery() { return freeDelivery; }
    public double getDeliveryCost() { return deliveryCost; }
    public String getDeliveryDetails() { return deliveryDetails; }


    // --- Сеттери ---
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setStatus(String status) { // Сеттер для статусу має також оновлювати стан
        this.status = status;
        this.currentState = createStateFromStatus(status);
    }
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? new ArrayList<>(imagePaths) : new ArrayList<>();
    }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    // setId не рекомендується, якщо adId - основний ідентифікатор, але якщо потрібно:
    // public void setId(Object id) { this.id = id; }


    // Сеттери для полів декораторів
    public void setPremium(boolean premium) { this.premium = premium; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public void setHasDiscount(boolean hasDiscount) { this.hasDiscount = hasDiscount; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public void setDiscountReason(String discountReason) { this.discountReason = discountReason; }

    public void setHasWarranty(boolean hasWarranty) { this.hasWarranty = hasWarranty; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
    public void setWarrantyType(String warrantyType) { this.warrantyType = warrantyType; }

    public void setHasDelivery(boolean hasDelivery) { this.hasDelivery = hasDelivery; }
    public void setFreeDelivery(boolean freeDelivery) { this.freeDelivery = freeDelivery; }
    public void setDeliveryCost(double deliveryCost) { this.deliveryCost = deliveryCost; }
    public void setDeliveryDetails(String deliveryDetails) { this.deliveryDetails = deliveryDetails; }


    // --- Клонування ---
    @Override
    public Ad clone() throws CloneNotSupportedException {
        Ad cloned = (Ad) super.clone();
        cloned.id = this.id; // Копіюємо поле id
        cloned.imagePaths = new ArrayList<>(this.imagePaths); // Глибоке копіювання списку шляхів
        cloned.currentState = createStateFromStatus(this.status); // Створюємо новий об'єкт стану

        // Копіювання значень для нових полів
        cloned.premium = this.premium;
        cloned.urgent = this.urgent;
        cloned.hasDiscount = this.hasDiscount;
        cloned.discountPercentage = this.discountPercentage;
        cloned.discountReason = this.discountReason;
        cloned.hasWarranty = this.hasWarranty;
        cloned.warrantyMonths = this.warrantyMonths;
        cloned.warrantyType = this.warrantyType;
        cloned.hasDelivery = this.hasDelivery;
        cloned.freeDelivery = this.freeDelivery;
        cloned.deliveryCost = this.deliveryCost;
        cloned.deliveryDetails = this.deliveryDetails;
        // createdAt копіюється автоматично при super.clone(), оскільки LocalDateTime immutable
        return cloned;
    }

    private AdState createStateFromStatus(String statusString) {
        if (statusString == null) return new DraftAdState();
        switch (statusString) {
            case "Чернетка": return new DraftAdState();
            case "Активне": return new ActiveAdState();
            case "Архівоване": return new ArchivedAdState();
            case "Продано": return new SoldAdState();
            case "На модерації": return new ModerationAdState();
            default: return new DraftAdState(); // За замовчуванням
        }
    }

    // --- equals, hashCode, toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return Objects.equals(adId, ad.adId); // Ідентифікація за adId
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
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 20)) + "..." : "N/A") + '\'' +
                ", price=" + price +
                ", categoryId='" + categoryId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", imagePathsCount=" + (imagePaths != null ? imagePaths.size() : 0) +
                ", premium=" + premium +
                ", urgent=" + urgent +
                ", hasDiscount=" + hasDiscount +
                (hasDiscount ? ", discountPercentage=" + discountPercentage + ", discountReason='" + discountReason + '\'' : "") +
                ", hasWarranty=" + hasWarranty +
                (hasWarranty ? ", warrantyMonths=" + warrantyMonths + ", warrantyType='" + warrantyType + '\'' : "") +
                ", hasDelivery=" + hasDelivery +
                (hasDelivery ? ", freeDelivery=" + freeDelivery + ", deliveryCost=" + deliveryCost + ", deliveryDetails='" + deliveryDetails + '\'' : "") +
                '}';
    }

    // Методи setAdId та setId не рекомендовані, якщо adId має бути незмінним після створення.
    // Якщо потрібно їх додати:
    // public void setAdId(String adId) { this.adId = adId; }
    // public void setId(Object id) { this.id = id; }

    // Залишив старий метод setState, якщо він десь використовується, хоча setCurrentState краще
    @Deprecated
    public void setState(AdState state) {
        this.currentState = state;
        updateStatus();
    }
}