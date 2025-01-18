package pl.dolien.echohub.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dolien.echohub.chat.dto.ChatResponse;
import pl.dolien.echohub.user.User;
import pl.dolien.echohub.user.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository repository;
    private final UserService userService;
    private final ChatMapper mapper;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiver(Authentication currentUser) {
        final String userId = currentUser.getName();
        return repository.findChatsBySenderId(userId)
                .stream()
                .map(chat -> mapper.toChatResponse(chat, userId))
                .toList();

    }

    @Transactional
    public String createChat(String senderId, String receiverId) {
        return repository.findChatByReceiverAndSender(senderId, receiverId)
                .map(Chat::getId)
                .orElseGet(() -> createAndSaveChat(senderId, receiverId));
    }

    public Chat getChatById(String chatId) {
        return repository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Chat not found."));
    }

    private String createAndSaveChat(String senderId, String receiverId) {
        User sender = userService.findUserByPublicId(senderId);
        User recipient = userService.findUserByPublicId(receiverId);

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(recipient);

        Chat savedChat = repository.save(chat);
        return savedChat.getId();
    }
}
