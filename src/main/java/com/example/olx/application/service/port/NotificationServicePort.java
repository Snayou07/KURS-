// com/example/olx/application/service/port/NotificationServicePort.java (для Observer)
package com.example.olx.application.service.port;

import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.User;

import java.util.List;

public interface NotificationServicePort {
    void notifyUsersAboutNewAd(Ad ad, List<User> interestedUsers); // Приклад
    void sendSystemMessage(String message); // Загальне повідомлення
}