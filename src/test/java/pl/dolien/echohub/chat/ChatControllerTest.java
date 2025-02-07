package pl.dolien.echohub.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.dolien.echohub.chat.dto.ChatResponse;
import pl.dolien.echohub.common.StringResponse;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private static final String CHAT_ID = "3";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private ChatController controller;
    @Mock
    private ChatService chatService;
    @Mock
    private Authentication auth;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldCreateChat() throws Exception {
        String senderId = "1";
        String receiverId = "2";
        StringResponse stringResponse = StringResponse.builder()
                .response(CHAT_ID)
                .build();

        when(chatService.createChat(senderId, receiverId)).thenReturn(CHAT_ID);

        performCreateChat(senderId, receiverId)
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(stringResponse)));

        verify(chatService, times(1)).createChat(senderId, receiverId);
    }

    @Test
    void shouldReturnChatsByReceiver() throws Exception {
        ChatResponse chatResponse = ChatResponse.builder()
                .id(CHAT_ID)
                .build();

        when(chatService.getChatsByReceiver(auth)).thenReturn(List.of(chatResponse));

        performGetChatsByReceiver()
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(chatResponse))));

        verify(chatService, times(1)).getChatsByReceiver(auth);
    }

    @Test
    void shouldAddToFavorite() throws Exception {
        performAddToFavorite(CHAT_ID)
                .andExpect(status().isOk());

        verify(chatService, times(1)).addToFavorite(CHAT_ID, auth.getName());
    }

    @Test
    void shouldRemoveFromFavorite() throws Exception {
        performRemoveFromFavorite(CHAT_ID)
                .andExpect(status().isOk());

        verify(chatService, times(1)).removeFromFavorite(CHAT_ID, auth.getName());
    }

    private ResultActions performGetChatsByReceiver() throws Exception {
        return mockMvc.perform(get("/chats")
                .principal(auth));
    }

    private ResultActions performCreateChat(String senderId, String receiverId) throws Exception {
        return mockMvc.perform(post("/chats")
                .param("sender-id", senderId)
                .param("receiver-id", receiverId));
    }

    private ResultActions performAddToFavorite(String chatId) throws Exception {
        return mockMvc.perform(post("/chats/{chatId}/favorite", chatId)
                .principal(auth));
    }

    private ResultActions performRemoveFromFavorite(String chatId) throws Exception {
        return mockMvc.perform(delete("/chats/{chatId}/favorite", chatId)
                .principal(auth));
    }
}