package com.slackish.message;

import java.time.Instant;

public class MessageDtos {
    public record CreateMessageRequest(String body, Long parentId) {}
    public record EditMessageRequest(String body) {}
    public record MessageView(Long id, Long channelId, Long userId, String body,
                              Long parentId, Instant createdAt, Instant editedAt) {}
}
