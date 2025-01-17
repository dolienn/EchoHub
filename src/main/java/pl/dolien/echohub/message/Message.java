package pl.dolien.echohub.message;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dolien.echohub.chat.Chat;
import pl.dolien.echohub.common.BaseAuditingEntity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static pl.dolien.echohub.message.MessageConstants.FIND_MESSAGES_BY_CHAT_ID;
import static pl.dolien.echohub.message.MessageConstants.SET_MESSAGES_TO_SEEN_BY_CHAT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
@NamedQuery(name = FIND_MESSAGES_BY_CHAT_ID,
            query = "SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdDate")
@NamedQuery(name = SET_MESSAGES_TO_SEEN_BY_CHAT,
            query = "UPDATE Message  SET state = :newState WHERE chat.id = :chatId")
public class Message extends BaseAuditingEntity {

    @Id
    @SequenceGenerator(name = "msg_seq", sequenceName = "msg_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "msg_seq")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(STRING)
    private MessageState state;

    @Enumerated(STRING)
    private MessageType type;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(name = "sender_id",nullable = false)
    private String senderId;

    @Column(name = "receiver_id",nullable = false)
    private String receiverId;
}
