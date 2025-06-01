package com.example.olx.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DraftAdState implements Serializable {
    private static final long serialVersionUID = 1L; // Важливо для версіонування

    private String title;
    private String description;
    private BigDecimal price;
    private String categoryId;
    private String location;
    private String contactPhone;
    private String contactEmail;
    private String imagePath;

    // Конструктори
    public DraftAdState() {
        // Порожній конструктор для серіалізації
    }

    public DraftAdState(String title, String description, BigDecimal price,
                        String categoryId, String location, String contactPhone,
                        String contactEmail, String imagePath) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.location = location;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.imagePath = imagePath;
    }

    // Getters and Setters
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "DraftAdState{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", categoryId='" + categoryId + '\'' +
                ", location='" + location + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}