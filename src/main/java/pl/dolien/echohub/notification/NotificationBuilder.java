package pl.dolien.echohub.notification;

import pl.dolien.echohub.chat.Chat;
import pl.dolien.echohub.message.MessageType;

public class NotificationBuilder {

    private NotificationBuilder() {}

    public static Notification buildNotification(
            Chat chat,
            String senderId,
            String receiverId,
            NotificationType type,
            String content,
            MessageType messageType,
            byte[] media
    ) {
        return Notification.builder()
                .chatId(chat.getId())
                .type(type)
                .messageType(messageType)
                .content(content)
                .senderId(senderId)
                .receiverId(receiverId)
                .chatName(chat.getTargetChatName(senderId))
                .media(media)
                .build();
    }
}
