package pl.dolien.echohub.message;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.dolien.echohub.chat.Chat;
import pl.dolien.echohub.chat.ChatService;
import pl.dolien.echohub.file.FileService;
import pl.dolien.echohub.message.dto.MessageRequest;
import pl.dolien.echohub.message.dto.MessageResponse;
import pl.dolien.echohub.notification.Notification;
import pl.dolien.echohub.notification.NotificationService;
import pl.dolien.echohub.notification.NotificationType;

import java.util.List;

import static pl.dolien.echohub.file.FileUtils.readFileFromLocation;
import static pl.dolien.echohub.message.MessageBuilder.buildMessage;
import static pl.dolien.echohub.message.MessageState.SEEN;
import static pl.dolien.echohub.message.MessageType.*;
import static pl.dolien.echohub.notification.NotificationBuilder.buildNotification;
import static pl.dolien.echohub.notification.NotificationType.MESSAGE;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository repository;
    private final ChatService chatService;
    private final FileService fileService;
    private final NotificationService notificationService;

    public void saveMessage(MessageRequest messageRequest) {
        Chat chat = chatService.getChatById(messageRequest.getChatId());

        buildAndSaveMessage(chat, messageRequest);

        Notification notification = buildNotificationToSend(chat, messageRequest);
        notificationService.sendNotification(messageRequest.getReceiverId(), notification);
    }

    public List<MessageResponse> getChatMessages(String chatId) {
        return repository.findMessagesByChatId(chatId)
                .stream()
                .map(MessageMapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public void setMessagesToSeen(String chatId, Authentication auth) {
        Chat chat = chatService.getChatById(chatId);

        final String recipientId = getRecipientId(chat, auth.getName());
        final String senderId = getSenderId(chat, auth.getName());

        repository.setMessagesToSeenByChat(chatId, SEEN);

        Notification notification = buildNotification(
                chat,
                senderId,
                recipientId,
                NotificationType.SEEN,
                null,
                null,
                null);
        notificationService.sendNotification(recipientId, notification);
    }

    public void uploadMediaMessage(
            String chatId,
            MultipartFile file,
            Authentication auth,
            String mediaType
    ) {
        Chat chat = chatService.getChatById(chatId);

        var messageType = getMessageType(mediaType);
        var notificationType = getNotificationType(mediaType);

        final String senderId = getSenderId(chat, auth.getName());
        final String recipientId = getRecipientId(chat, auth.getName());
        final String filePath = fileService.saveFile(file, senderId);

        Message message = buildMessage(
                chat,
                senderId,
                recipientId,
                messageType,
                null,
                filePath
        );
        repository.save(message);

        Notification notification = buildNotification(
                chat,
                senderId,
                recipientId,
                notificationType,
                null,
                messageType,
                readFileFromLocation(filePath));
        notificationService.sendNotification(recipientId, notification);
    }

    private MessageType getMessageType(String fileType) {
        switch (fileType) {
            case "AUDIO" -> {
                return AUDIO;
            }
            case "IMAGE" -> {
                return IMAGE;
            }
            case "VIDEO" -> {
                return VIDEO;
            }
            default -> throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }
    }

    private NotificationType getNotificationType(String fileType){
        switch (fileType) {
            case "AUDIO" -> {
                return NotificationType.AUDIO;
            }
            case "IMAGE" -> {
                return NotificationType.IMAGE;
            }
            case "VIDEO" -> {
                return NotificationType.VIDEO;
            }
            default -> throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }
    }

    private void buildAndSaveMessage(Chat chat, MessageRequest messageRequest) {
        Message message = buildMessage(
                chat,
                messageRequest.getSenderId(),
                messageRequest.getReceiverId(),
                messageRequest.getType(),
                messageRequest.getContent(),
                null
        );
        repository.save(message);
    }

    public Notification buildNotificationToSend(Chat chat, MessageRequest messageRequest) {
        return buildNotification(
                chat,
                messageRequest.getSenderId(),
                messageRequest.getReceiverId(),
                MESSAGE,
                messageRequest.getContent(),
                messageRequest.getType(),
                null);
    }

    private String getSenderId(Chat chat, String authName) {
        String chatSenderId = chat.getSender().getId();
        String chatRecipientId = chat.getRecipient().getId();

        return chatSenderId.equals(authName)
                ? chatSenderId
                : chatRecipientId;
    }

    private String getRecipientId(Chat chat, String authName) {
        String chatSenderId = chat.getSender().getId();
        String chatRecipientId = chat.getRecipient().getId();

        return chatSenderId.equals(authName)
                ? chatRecipientId
                : chatSenderId;
    }
}
