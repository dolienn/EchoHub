package pl.dolien.echohub.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.dolien.echohub.message.MessageType.IMAGE;

class MessageControllerTest {

    private static final String CHAT_ID = "1";
    @InjectMocks
    private MessageController controller;
    @Mock
    private MessageService service;
    @Mock
    private Authentication auth;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldUploadMediaMessage() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        performUploadMedia(file)
                .andExpect(status().isCreated());

        verify(service, times(1)).uploadMediaMessage(
                anyString(),
                any(MultipartFile.class),
                any(Authentication.class),
                any(MessageType.class)
        );
    }

    @Test
    void shouldSetMessagesToSeen() throws Exception {
        performSetMessagesToSeen()
                .andExpect(status().isAccepted());

        verify(service, times(1)).setMessagesToSeen(anyString(), any(Authentication.class));
    }

    @Test
    void shouldReturnMessagesByChatId() throws Exception {
        performReturnMessages()
                .andExpect(status().isOk());
    }

    private ResultActions performUploadMedia(MultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/messages/upload-file")
                .file("file", file.getBytes())
                .param("chat-id", CHAT_ID)
                .param("media-type", String.valueOf(IMAGE))
                .principal(auth)
                .contentType(MULTIPART_FORM_DATA));
    }

    private ResultActions performSetMessagesToSeen() throws Exception {
        return mockMvc.perform(patch("/messages")
                .param("chat-id", CHAT_ID)
                .principal(auth));
    }

    private ResultActions performReturnMessages() throws Exception {
        return mockMvc.perform(get("/messages/chat/{chat-id}", CHAT_ID));
    }
}