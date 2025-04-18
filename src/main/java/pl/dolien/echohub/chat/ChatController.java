package pl.dolien.echohub.chat;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.dolien.echohub.chat.dto.ChatResponse;
import pl.dolien.echohub.common.StringResponse;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public StringResponse createChat(
            @RequestParam(name = "sender-id") String senderId,
            @RequestParam(name = "receiver-id") String receiverId
    ) {
        final String chatId = chatService.createChat(senderId, receiverId);
        return StringResponse.builder()
                .response(chatId)
                .build();
    }

    @GetMapping
    public List<ChatResponse> getChatsByReceiver(Authentication auth) {
        return chatService.getChatsByReceiver(auth);
    }
}
