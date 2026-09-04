package com.slackish.dm;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DmMessageRepository extends JpaRepository<DmMessage, Long> {
    List<DmMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
