package com.slackish.channel;

import com.slackish.common.Errors;
import com.slackish.common.Uid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelRepository channels;

    public ChannelController(ChannelService channelService, ChannelRepository channels) {
        this.channelService = channelService;
        this.channels = channels;
    }

    @PostMapping("/workspaces/{workspaceId}/channels")
    public ChannelDtos.ChannelView create(@PathVariable Long workspaceId,
                                          @RequestBody ChannelDtos.CreateChannelRequest req, @Uid Long uid) {
        Channel c = channelService.create(workspaceId, req.name(), req.isPrivate(), uid);
        return view(c);
    }

    @GetMapping("/workspaces/{workspaceId}/channels")
    public List<ChannelDtos.ChannelView> list(@PathVariable Long workspaceId, @Uid Long uid) {
        return channelService.listForWorkspace(workspaceId, uid).stream().map(this::view).toList();
    }

    @PostMapping("/channels/{channelId}/join")
    public void join(@PathVariable Long channelId, @Uid Long uid) {
        channelService.join(channelId, uid);
    }

    @DeleteMapping("/channels/{channelId}")
    public void delete(@PathVariable Long channelId, @Uid Long uid) {
        channelService.delete(channelId);
    }

    private ChannelDtos.ChannelView view(Channel c) {
        return new ChannelDtos.ChannelView(c.getId(), c.getWorkspaceId(), c.getName(), c.isPrivate(), c.getCreatedBy());
    }
}
