package com.example.olx.presentation.gui;

public class AdDisplayConfig {
    private boolean premium;
    private boolean urgent;
    private Double discountPercentage;
    private String discountReason;
    private Integer warrantyMonths;
    private String warrantyType;
    private Boolean freeDelivery;
    private Double deliveryCost;
    private String deliveryInfo;

    // Конструктори
    public AdDisplayConfig() {}

    public AdDisplayConfig(boolean premium, boolean urgent, Double discountPercentage,
                           String discountReason, Integer warrantyMonths, String warrantyType,
                           Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        this.premium = premium;
        this.urgent = urgent;
        this.discountPercentage = discountPercentage;
        this.discountReason = discountReason;
        this.warrantyMonths = warrantyMonths;
        this.warrantyType = warrantyType;
        this.freeDelivery = freeDelivery;
        this.deliveryCost = deliveryCost;
        this.deliveryInfo = deliveryInfo;
    }

    // Статичний метод для створення дефолтної конфігурації
    public static AdDisplayConfig defaultConfig() {
        return new AdDisplayConfig(false, false, null, null, null, null, null, null, null);
    }

    // Getters and Setters
    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getDiscountReason() { return discountReason; }
    public void setDiscountReason(String discountReason) { this.discountReason = discountReason; }

    public Integer getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(Integer warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    public String getWarrantyType() { return warrantyType; }
    public void setWarrantyType(String warrantyType) { this.warrantyType = warrantyType; }

    public Boolean getFreeDelivery() { return freeDelivery; }
    public void setFreeDelivery(Boolean freeDelivery) { this.freeDelivery = freeDelivery; }

    public Double getDeliveryCost() { return deliveryCost; }
    public void setDeliveryCost(Double deliveryCost) { this.deliveryCost = deliveryCost; }

    public String getDeliveryInfo() { return deliveryInfo; }
    public void setDeliveryInfo(String deliveryInfo) { this.deliveryInfo = deliveryInfo; }
}