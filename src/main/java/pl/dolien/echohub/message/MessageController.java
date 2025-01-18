package pl.dolien.echohub.message;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.dolien.echohub.message.dto.MessageRequest;
import pl.dolien.echohub.message.dto.MessageResponse;

import java.util.List;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService service;

    @PostMapping
    @ResponseStatus(CREATED)
    public void saveMessage(@RequestBody MessageRequest message) {
        service.saveMessage(message);
    }

    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
    @ResponseStatus(CREATED)
    public void uploadMedia(
            @RequestParam("chat-id") String chatId,
            // todo add @Parameter from swagger
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        service.uploadMediaMessage(chatId, file, auth);
    }

    @PatchMapping
    @ResponseStatus(ACCEPTED)
    public void setMessagesToSeen(
            @RequestParam("chat-id") String chatId,
            Authentication auth
    ) {
        service.setMessagesToSeen(chatId, auth);
    }

    @GetMapping("/chat/{chat-id}")
    public List<MessageResponse> getMessages(
            @PathVariable("chat-id") String chatId
    ) {
        return service.getChatMessages(chatId);
    }
}
