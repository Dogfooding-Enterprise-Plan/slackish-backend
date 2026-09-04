package com.slackish.channel;

import com.slackish.common.Errors;
import com.slackish.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {

    private final ChannelRepository channels;
    private final ChannelMemberRepository channelMembers;
    private final WorkspaceService workspaceService;

    public ChannelService(ChannelRepository channels, ChannelMemberRepository channelMembers,
                          WorkspaceService workspaceService) {
        this.channels = channels;
        this.channelMembers = channelMembers;
        this.workspaceService = workspaceService;
    }

    public Channel create(Long workspaceId, String name, boolean isPrivate, Long creatorId) {
        workspaceService.requireMember(workspaceId, creatorId);
        Channel c = new Channel();
        c.setWorkspaceId(workspaceId);
        c.setName(name);
        c.setPrivate(isPrivate);
        c.setCreatedBy(creatorId);
        Channel saved = channels.save(c);
        channelMembers.save(new ChannelMember(saved.getId(), creatorId));
        return saved;
    }

    public List<Channel> listForWorkspace(Long workspaceId, Long userId) {
        workspaceService.requireMember(workspaceId, userId);
        return channels.findByWorkspaceId(workspaceId).stream()
                .filter(c -> !c.isPrivate() || channelMembers.existsByChannelIdAndUserId(c.getId(), userId))
                .toList();
    }

    public ChannelMember join(Long channelId, Long userId) {
        Channel c = channels.findById(channelId).orElseThrow(() -> Errors.notFound("Channel not found"));
        return channelMembers.findByChannelIdAndUserId(channelId, userId)
                .orElseGet(() -> channelMembers.save(new ChannelMember(c.getId(), userId)));
    }

    public void delete(Long channelId) {
        Channel c = channels.findById(channelId).orElseThrow(() -> Errors.notFound("Channel not found"));
        channels.delete(c);
    }
}
