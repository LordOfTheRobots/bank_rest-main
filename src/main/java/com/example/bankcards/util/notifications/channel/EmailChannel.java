package com.example.bankcards.util.notifications.channel;

import com.example.bankcards.entity.DeliveryChannel;
import com.example.bankcards.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel{

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public DeliveryChannel getChannelType() {
        return DeliveryChannel.EMAIL;
    }

    @Override
    public void send(String address, String title, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress);
        mail.setTo(address);
        mail.setSubject(title);
        mail.setText(message);
        mailSender.send(mail);
    }
}
