package com.slackish.message;

import com.slackish.channel.ChannelMemberRepository;
import com.slackish.common.Errors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messages;
    private final ChannelMemberRepository channelMembers;

    public MessageService(MessageRepository messages, ChannelMemberRepository channelMembers) {
        this.messages = messages;
        this.channelMembers = channelMembers;
    }

    public Message post(Long channelId, Long userId, String body, Long parentId) {
        Message m = new Message();
        m.setChannelId(channelId);
        m.setUserId(userId);
        m.setBody(body);
        m.setParentId(parentId);
        return messages.save(m);
    }

    public Message getById(Long id) {
        return messages.findById(id).orElseThrow(() -> Errors.notFound("Message not found"));
    }

    public List<Message> listChannel(Long channelId, Long userId) {
        if (!channelMembers.existsByChannelIdAndUserId(channelId, userId)) {
            throw Errors.forbidden("Not a member of this channel");
        }
        return messages.findByChannelIdOrderByCreatedAtAsc(channelId, PageRequest.of(0, 50));
    }

    public List<Message> thread(Long parentId, Long userId) {
        Message parent = getById(parentId);
        if (!channelMembers.existsByChannelIdAndUserId(parent.getChannelId(), userId)) {
            throw Errors.forbidden("Not a member of this channel");
        }
        return messages.findByParentIdOrderByCreatedAtAsc(parentId);
    }

    public Message edit(Long id, Long userId, String body) {
        Message m = getById(id);
        if (!m.getUserId().equals(userId)) {
            throw Errors.forbidden("You can only edit your own messages");
        }
        m.setBody(body);
        m.setEditedAt(Instant.now());
        return messages.save(m);
    }
}
