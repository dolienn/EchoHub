package pl.dolien.echohub.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dolien.echohub.chat.dto.ChatResponse;
import pl.dolien.echohub.user.User;
import pl.dolien.echohub.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CHAT_NOT_FOUND_MSG = "Chat not found.";
    private final ChatRepository repository;
    private final UserService userService;
    private final ChatMapper mapper;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiver(Authentication currentUser) {
        final String userId = currentUser.getName();
        User user = userService.findUserByPublicId(userId);
        user.setLastSeen(LocalDateTime.now());
        userService.saveUser(user);

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
        return repository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT_NOT_FOUND_MSG));
    }

    public void addToFavorite(String chatId, String userId) {
        Chat chat = getChatById(chatId);
        chat.addFavorite(userId);
        repository.save(chat);
    }

    public void removeFromFavorite(String chatId, String userId) {
        Chat chat = getChatById(chatId);
        chat.removeFavorite(userId);
        repository.save(chat);
    }

    public boolean isFavoriteForUser(String chatId, String userId) {
        Chat chat = getChatById(chatId);
        return chat.isFavoriteForUser(userId);
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
