package pl.dolien.echohub.message;

import pl.dolien.echohub.chat.Chat;

import static pl.dolien.echohub.message.MessageState.SENT;

public class MessageBuilder {

    private MessageBuilder() {}

    public static Message buildMessage(
            Chat chat,
            String senderId,
            String receiverId,
            MessageType type,
            String content,
            String mediaFilePath
    ) {
        return Message.builder()
                .chat(chat)
                .senderId(senderId)
                .receiverId(receiverId)
                .type(type)
                .state(SENT)
                .content(content)
                .mediaFilePath(mediaFilePath)
                .build();
    }

}
