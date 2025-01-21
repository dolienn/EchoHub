package pl.dolien.echohub.chat;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.dolien.echohub.common.BaseAuditingEntity;
import pl.dolien.echohub.message.Message;
import pl.dolien.echohub.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.UUID;
import static pl.dolien.echohub.chat.ChatConstants.FIND_CHAT_BY_SENDER_ID;
import static pl.dolien.echohub.chat.ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER;
import static pl.dolien.echohub.message.MessageState.SENT;
import static pl.dolien.echohub.message.MessageType.TEXT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "chat")
@NamedQuery(name = FIND_CHAT_BY_SENDER_ID,
            query = "SELECT DISTINCT c FROM Chat c WHERE c.sender.id = :senderId OR c.recipient.id = :senderId ORDER BY createdDate DESC")
@NamedQuery(name = FIND_CHAT_BY_SENDER_ID_AND_RECEIVER,
            query = "SELECT DISTINCT c from Chat c WHERE  (c.sender.id = :senderId AND c.recipient.id = :recipientId) OR (c.sender.id = :recipientId AND c.recipient.id = :senderId)")
public class Chat extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @OneToMany(mappedBy = "chat", fetch = EAGER)
    @OrderBy("createdDate DESC")
    private List<Message> messages;

    @Transient
    public String getChatName(final String senderId) {
        return recipient.getId().equals(senderId)
                ? sender.getFirstName() + " " + sender.getLastName()
                : recipient.getFirstName() + " " + recipient.getLastName();
    }

    @Transient
    public String getTargetChatName(final String senderId) {
        return sender.getId().equals(senderId)
                ? sender.getFirstName() + " " + sender.getLastName()
                : recipient.getFirstName() + " " + recipient.getLastName();
    }

    @Transient
    public long getUnreadMessages(final String senderId) {
        return messages
                .stream()
                .filter(m -> m.getReceiverId().equals(senderId))
                .filter(m -> SENT == m.getState())
                .count();
    }

    @Transient
    public String getLastMessage() {
        if(messages != null && !messages.isEmpty()) {
            Message lastMessage = messages.get(0);
            return lastMessage.getType() != TEXT
                    ? "Attachment"
                    : lastMessage.getContent();
        }
        return null;
    }

    @Transient
    public LocalDateTime getLastMessageTime() {
        if(messages != null && !messages.isEmpty()) {
            return messages.get(0).getCreatedDate();
        }
        return null;
    }
}
