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

import java.util.List;

import static pl.dolien.echohub.message.MessageState.SEEN;
import static pl.dolien.echohub.message.MessageState.SENT;
import static pl.dolien.echohub.message.MessageType.IMAGE;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository repository;
    private final ChatService chatService;
    private final FileService fileService;

    public void saveMessage(MessageRequest messageRequest) {
        Chat chat = chatService.getChatById(messageRequest.getChatId());

        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(message.getSenderId());
        message.setReceiverId(message.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(SENT);
        repository.save(message);

        // todo notification
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

        // final String recipientId = getRecipientId(chat, auth.getName());

        repository.setMessagesToSeenByChat(chatId, SEEN);

        // todo notification
    }

    public void uploadMediaMessage(String chatId,
                                   MultipartFile file,
                                   Authentication auth
    ) {
        Chat chat = chatService.getChatById(chatId);

        final String senderId = getSenderId(chat, auth.getName());
        final String recipientId = getRecipientId(chat, auth.getName());

        final String filePath = fileService.saveFile(file, senderId);

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(recipientId);
        message.setType(IMAGE);
        message.setState(SENT);
        message.setMediaFilePath(filePath);
        repository.save(message);

        //todo notification
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
