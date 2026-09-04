package com.slackish.channel;

public class ChannelDtos {
    public record CreateChannelRequest(String name, boolean isPrivate) {}
    public record ChannelView(Long id, Long workspaceId, String name, boolean isPrivate, Long createdBy) {}
}
