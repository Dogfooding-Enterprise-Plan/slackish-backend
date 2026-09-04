package com.slackish.dm;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DmConversationRepository extends JpaRepository<DmConversation, Long> {
    List<DmConversation> findByUserAIdOrUserBId(Long a, Long b);
    List<DmConversation> findByWorkspaceId(Long workspaceId);
}
