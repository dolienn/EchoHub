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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

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
        String chatId = "3";
        StringResponse stringResponse = StringResponse.builder()
                .response(chatId)
                .build();

        when(chatService.createChat(senderId, receiverId)).thenReturn(chatId);

        performCreateChat(senderId, receiverId)
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(stringResponse)));

        verify(chatService, times(1)).createChat(senderId, receiverId);
    }

    @Test
    void shouldReturnChatsByReceiver() throws Exception {
        ChatResponse chatResponse = ChatResponse.builder()
                .id("1")
                .build();

        when(chatService.getChatsByReceiver(auth)).thenReturn(List.of(chatResponse));

        performGetChatsByReceiver()
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(chatResponse))));
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
}