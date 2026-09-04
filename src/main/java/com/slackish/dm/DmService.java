package com.slackish.dm;

import com.slackish.common.Errors;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DmService {

    private final DmConversationRepository conversations;
    private final DmMessageRepository dmMessages;

    public DmService(DmConversationRepository conversations, DmMessageRepository dmMessages) {
        this.conversations = conversations;
        this.dmMessages = dmMessages;
    }

    public List<DmConversation> forUser(Long userId) {
        return conversations.findByUserAIdOrUserBId(userId, userId);
    }

    public DmConversation create(Long workspaceId, Long userId, Long otherUserId) {
        DmConversation c = new DmConversation();
        c.setWorkspaceId(workspaceId);
        c.setUserAId(userId);
        c.setUserBId(otherUserId);
        return conversations.save(c);
    }

    public DmConversation get(Long conversationId) {
        return conversations.findById(conversationId)
                .orElseThrow(() -> Errors.notFound("Conversation not found"));
    }

    public List<DmMessage> messages(Long conversationId) {
        return dmMessages.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public DmMessage send(Long conversationId, Long senderId, String body) {
        DmConversation c = get(conversationId);
        if (!c.getUserAId().equals(senderId) && !c.getUserBId().equals(senderId)) {
            throw Errors.forbidden("Not a participant in this conversation");
        }
        DmMessage m = new DmMessage();
        m.setConversationId(conversationId);
        m.setSenderId(senderId);
        m.setBody(body);
        return dmMessages.save(m);
    }
}
