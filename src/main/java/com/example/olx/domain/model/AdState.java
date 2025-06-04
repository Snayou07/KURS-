
// src/main/java/com/example/olx/domain/model/AdState.java
package com.example.olx.domain.model;

public interface AdState {
    void publish(Ad ad);
    void archive(Ad ad);
    void markAsSold(Ad ad);
    String getStatusName();


    public enum adState {
        ACTIVE,           // Активное объявление
        INACTIVE,         // Неактивное объявление
        PENDING_APPROVAL, // Ожидает модерации
        REJECTED,         // Отклонено модератором
        SOLD,            // Продано
        EXPIRED,         // Истек срок
        DELETED          // Удалено
    }
}