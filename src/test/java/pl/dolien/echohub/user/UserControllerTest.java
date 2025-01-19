package pl.dolien.echohub.user;

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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private UserController controller;
    @Mock
    private UserService service;
    @Mock
    private Authentication auth;
    private MockMvc mockMvc;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        initUserResponse();
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        when(service.getAllUsersExceptSelf(auth)).thenReturn(List.of(userResponse));

        performGetAllUsers()
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(userResponse))));

        verify(service, times(1)).getAllUsersExceptSelf(auth);
    }

    private ResultActions performGetAllUsers() throws Exception {
        return mockMvc.perform(get("/users")
                .principal(auth));
    }

    private void initUserResponse() {
        userResponse = UserResponse.builder()
                .id("1")
                .build();
    }
}