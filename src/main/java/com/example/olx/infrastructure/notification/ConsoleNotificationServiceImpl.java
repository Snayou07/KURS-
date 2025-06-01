// com/example/olx/infrastructure/notification/ConsoleNotificationServiceImpl.java (реалізація Observer)
package com.example.olx.infrastructure.notification;

import com.example.olx.application.service.port.NotificationServicePort;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.User;
import java.util.List;

public abstract class ConsoleNotificationServiceImpl implements NotificationServicePort {
    @Override
    public void notifyUsersAboutNewAd(Ad ad, List<User> interestedUsers) {
        System.out.println("[NOTIFICATION] New Ad Posted: " + ad.getTitle());
        if (interestedUsers != null && !interestedUsers.isEmpty()) {
            interestedUsers.forEach(user ->
                    System.out.println("  > Notifying user: " + user.getUsername() + " about ad '" + ad.getTitle() + "'")
            );
        }
    }

    @Override
    public void sendSystemMessage(String message) {
        System.out.println("[SYSTEM NOTIFICATION] " + message);
    }
}