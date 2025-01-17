package pl.dolien.echohub.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static pl.dolien.echohub.chat.ChatConstants.FIND_CHAT_BY_SENDER_ID;
import static pl.dolien.echohub.chat.ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER;

public interface ChatRepository extends JpaRepository<Chat, String> {
    @Query(name = FIND_CHAT_BY_SENDER_ID)
    List<Chat> findChatsBySenderId(@Param("senderId") String userId);

    @Query(name = FIND_CHAT_BY_SENDER_ID_AND_RECEIVER)
    Optional<Chat> findChatByReceiverAndSender(@Param("senderId") String senderId,
                                               @Param("recipientId") String receiverId);
}
