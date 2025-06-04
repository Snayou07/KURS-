
// src/main/java/com/example/olx/domain/model/AdStateType.java
package com.example.olx.domain.model;

public enum AdStateType {
    ACTIVE,           // Активное объявление
    INACTIVE,         // Неактивное объявление
    PENDING_APPROVAL, // Ожидает модерации
    REJECTED,         // Отклонено модератором
    SOLD,            // Продано
    EXPIRED,         // Истек срок
    DELETED,         // Удалено
    DRAFT,           // Черновик
    ARCHIVED         // Архивировано
}