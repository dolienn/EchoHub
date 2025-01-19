package pl.dolien.echohub.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import pl.dolien.echohub.chat.Chat;
import pl.dolien.echohub.chat.ChatService;
import pl.dolien.echohub.file.FileService;
import pl.dolien.echohub.message.dto.MessageRequest;
import pl.dolien.echohub.message.dto.MessageResponse;
import pl.dolien.echohub.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dolien.echohub.message.MessageMapper.toMessageResponse;
import static pl.dolien.echohub.message.MessageState.SEEN;
import static pl.dolien.echohub.message.MessageState.SENT;
import static pl.dolien.echohub.message.MessageType.TEXT;

class MessageServiceTest {

    private static final String SENDER_ID = "1";
    private static final String RECEIVER_ID = "2";
    private static final String CHAT_ID = "3";
    @InjectMocks
    private MessageService messageService;
    @Mock
    private MessageRepository repository;
    @Mock
    private ChatService chatService;
    @Mock
    private FileService fileService;
    @Mock
    private Authentication auth;
    private Chat chat;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        initData();
    }

    @Test
    void shouldSaveMessage() {
        MessageRequest messageRequest = getMessageRequest();

        when(chatService.getChatById(CHAT_ID)).thenReturn(chat);

        messageService.saveMessage(messageRequest);

        // todo asserts
        verify(repository, times(1)).save(any(Message.class));
        verify(chatService, times(1)).getChatById(CHAT_ID);
    }

    @Test
    void shouldGetChatMessages() {
        Message message = getTextMessage();

        when(repository.findMessagesByChatId(CHAT_ID)).thenReturn(List.of(message));

        List<MessageResponse> expected = List.of(toMessageResponse(message));
        List<MessageResponse> result = messageService.getChatMessages(CHAT_ID);

        assertMessageResponseEquals(expected.get(0), result.get(0));
        verify(repository, times(1)).findMessagesByChatId(CHAT_ID);
    }

    @Test
    void shouldSetMessagesToSeen() {
        when(chatService.getChatById(CHAT_ID)).thenReturn(chat);

        messageService.setMessagesToSeen(CHAT_ID, auth);

        // todo asserts
        verify(chatService, times(1)).getChatById(CHAT_ID);
        verify(repository, times(1)).setMessagesToSeenByChat(CHAT_ID, SEEN);
    }

    @Test
    void shouldUploadMediaMessage() {
        MultipartFile file = mock(MultipartFile.class);
        when(chatService.getChatById(CHAT_ID)).thenReturn(chat);
        when(fileService.saveFile(file, SENDER_ID)).thenReturn("filePath");
        when(auth.getName()).thenReturn(SENDER_ID);

        messageService.uploadMediaMessage(CHAT_ID, file, auth);

        // todo asserts
        verify(chatService, times(1)).getChatById(CHAT_ID);
        verify(fileService, times(1)).saveFile(file, SENDER_ID);
        verify(auth, times(2)).getName();
        verify(repository, times(1)).save(any(Message.class));
    }

    private void initData() {
        chat = Chat.builder()
                .id(CHAT_ID)
                .sender(User.builder().id(SENDER_ID).build())
                .recipient(User.builder().id(RECEIVER_ID).build())
                .messages(List.of(getTextMessage()))
                .build();
    }

    private MessageRequest getMessageRequest() {
        return MessageRequest.builder()
                .content("content")
                .senderId(SENDER_ID)
                .receiverId(RECEIVER_ID)
                .type(TEXT)
                .chatId(CHAT_ID)
                .build();
    }

    private Message getTextMessage() {
        return Message.builder()
                .id(1L)
                .content("content")
                .senderId(SENDER_ID)
                .receiverId(RECEIVER_ID)
                .type(TEXT)
                .state(SENT)
                .build();
    }

    private void assertMessageResponseEquals(MessageResponse expected, MessageResponse actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getContent(), actual.getContent());
        assertEquals(expected.getSenderId(), actual.getSenderId());
        assertEquals(expected.getReceiverId(), actual.getReceiverId());
        assertEquals(expected.getType(), actual.getType());
    }
}