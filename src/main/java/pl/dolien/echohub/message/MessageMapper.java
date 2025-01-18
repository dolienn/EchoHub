package pl.dolien.echohub.message;

import pl.dolien.echohub.message.dto.MessageResponse;

import static pl.dolien.echohub.file.FileUtils.readFileFromLocation;

public class MessageMapper {

    private MessageMapper() {}

    public static MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .type(message.getType())
                .state(message.getState())
                .createdAt(message.getCreatedDate())
                .media(readFileFromLocation(message.getMediaFilePath()))
                .build();
    }
}
