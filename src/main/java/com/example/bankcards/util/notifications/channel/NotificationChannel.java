package com.example.bankcards.util.notifications.channel;

import com.example.bankcards.entity.DeliveryChannel;
import com.example.bankcards.entity.User;

public interface NotificationChannel {
    void send(String address, String title, String message);
    DeliveryChannel getChannelType();
}
