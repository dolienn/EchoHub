package pl.dolien.echohub.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static pl.dolien.echohub.message.MessageConstants.FIND_MESSAGES_BY_CHAT_ID;
import static pl.dolien.echohub.message.MessageConstants.SET_MESSAGES_TO_SEEN_BY_CHAT;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(name = FIND_MESSAGES_BY_CHAT_ID)
    List<Message> findMessagesByChatId(String chatId);

    @Query(name = SET_MESSAGES_TO_SEEN_BY_CHAT)
    @Modifying
    void setMessagesToSeenByChat(@Param("chatId") String chatId,
                                 @Param("newState") MessageState state);
}
