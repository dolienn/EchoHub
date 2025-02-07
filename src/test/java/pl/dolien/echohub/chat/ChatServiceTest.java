package pl.dolien.echohub.chat;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import pl.dolien.echohub.chat.dto.ChatResponse;
import pl.dolien.echohub.user.User;
import pl.dolien.echohub.user.UserService;

import java.util.HashSet;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private static final String SENDER_ID = "1";
    private static final String RECIPIENT_ID = "2";
    private static final String CHAT_NOT_FOUND_MSG = "Chat not found.";
    @InjectMocks
    private ChatService chatService;
    @Mock
    private ChatRepository repository;
    @Mock
    private UserService userService;
    @Mock
    private ChatMapper mapper;
    @Mock
    private Authentication authentication;
    private Chat chat;
    private ChatResponse chatResponse;
    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        initData();
    }

    @Test
    void shouldReturnChatsByReceiver() {
        mockGetChatsByReceiver();

        List<ChatResponse> result = chatService.getChatsByReceiver(authentication);

        assertEquals(List.of(chatResponse), result);
        verifyGetChatsByReceiver();
    }

    @Test
    void shouldCreateNewChatAndReturnChatId() {
        mockCreateNewChat();

        String result = chatService.createChat(SENDER_ID, RECIPIENT_ID);

        assertEquals("1", result);
        verifyCreateNewChat();
    }

    @Test
    void shouldReturnExistingChatId() {
        when(repository.findChatByReceiverAndSender(SENDER_ID, RECIPIENT_ID)).thenReturn(of(chat));

        String result = chatService.createChat(SENDER_ID, RECIPIENT_ID);

        assertEquals("1", result);
        verify(repository, times(1)).findChatByReceiverAndSender(SENDER_ID, RECIPIENT_ID);
    }

    @Test
    void shouldReturnChatById() {
        when(repository.findById(chat.getId())).thenReturn(of(chat));

        Chat result = chatService.getChatById(chat.getId());

        assertEquals(chat, result);
        verify(repository, times(1)).findById(chat.getId());
    }

    @Test
    void shouldThrowExceptionWhenChatNotFound() {
        when(repository.findById(chat.getId())).thenReturn(empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> chatService.getChatById(chat.getId()));

        assertEquals(CHAT_NOT_FOUND_MSG, exception.getMessage());
        verify(repository, times(1)).findById(chat.getId());
    }

    @Test
    void shouldAddToFavorite() {
        when(repository.findById(chat.getId())).thenReturn(of(chat));

        chatService.addToFavorite(chat.getId(), SENDER_ID);

        verify(repository, times(1)).findById(chat.getId());
        verify(repository, times(1)).save(chat);
    }

    @Test
    void shouldRemoveFromFavorite() {
        when(repository.findById(chat.getId())).thenReturn(of(chat));

        chatService.removeFromFavorite(chat.getId(), SENDER_ID);

        verify(repository, times(1)).findById(chat.getId());
        verify(repository, times(1)).save(chat);
    }

    @Test
    void shouldReturnTrueWhenChatIsFavoriteForUser() {
        when(repository.findById(chat.getId())).thenReturn(of(chat));

        boolean result = chatService.isFavoriteForUser(chat.getId(), SENDER_ID);

        assertTrue(result);
        verify(repository, times(1)).findById(chat.getId());
    }

    private void initData() {
        sender = User.builder().id(SENDER_ID).build();
        recipient = User.builder().id(RECIPIENT_ID).build();

        chat = Chat.builder()
                .id("1")
                .sender(sender)
                .recipient(recipient)
                .favoriteForUsers(new HashSet<>(List.of(SENDER_ID)))
                .build();

        chatResponse = ChatResponse.builder()
                .id("1")
                .name("name")
                .unreadCount(1)
                .lastMessage("message")
                .isRecipientOnline(true)
                .senderId(SENDER_ID)
                .receiverId(RECIPIENT_ID)
                .build();
    }

    private void mockGetChatsByReceiver() {
        when(authentication.getName()).thenReturn(SENDER_ID);
        when(repository.findChatsBySenderId(SENDER_ID)).thenReturn(List.of(chat));
        when(userService.findUserByPublicId(SENDER_ID)).thenReturn(sender);
        when(mapper.toChatResponse(chat, SENDER_ID)).thenReturn(chatResponse);
    }

    private void verifyGetChatsByReceiver() {
        verify(authentication, times(1)).getName();
        verify(repository, times(1)).findChatsBySenderId(SENDER_ID);
        verify(userService, times(1)).findUserByPublicId(SENDER_ID);
        verify(userService, times(1)).saveUser(sender);
        verify(mapper, times(1)).toChatResponse(chat, SENDER_ID);
    }

    private void mockCreateNewChat() {
        when(repository.findChatByReceiverAndSender(SENDER_ID, RECIPIENT_ID)).thenReturn(empty());
        when(userService.findUserByPublicId(SENDER_ID))
                .thenReturn(sender)
                .thenReturn(recipient);
        when(repository.save(any(Chat.class))).thenReturn(chat);
    }

    private void verifyCreateNewChat() {
        verify(repository, times(1)).findChatByReceiverAndSender(SENDER_ID, RECIPIENT_ID);
        verify(userService, times(1)).findUserByPublicId(SENDER_ID);
        verify(userService, times(1)).findUserByPublicId(RECIPIENT_ID);
        verify(repository, times(1)).save(any(Chat.class));
    }
}