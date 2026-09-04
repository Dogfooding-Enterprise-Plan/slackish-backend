package com.slackish.message;

import com.slackish.common.Uid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/channels/{channelId}/messages")
    public MessageDtos.MessageView create(@PathVariable Long channelId,
                                          @RequestBody MessageDtos.CreateMessageRequest req, @Uid Long uid) {
        return view(messageService.post(channelId, uid, req.body(), req.parentId()));
    }

    @GetMapping("/channels/{channelId}/messages")
    public List<MessageDtos.MessageView> list(@PathVariable Long channelId, @Uid Long uid) {
        return messageService.listChannel(channelId, uid).stream().map(this::view).toList();
    }

    @GetMapping("/messages/{id}")
    public MessageDtos.MessageView get(@PathVariable Long id, @Uid Long uid) {
        return view(messageService.getById(id));
    }

    @GetMapping("/messages/{id}/thread")
    public List<MessageDtos.MessageView> thread(@PathVariable Long id, @Uid Long uid) {
        return messageService.thread(id, uid).stream().map(this::view).toList();
    }

    @PutMapping("/messages/{id}")
    public MessageDtos.MessageView edit(@PathVariable Long id,
                                        @RequestBody MessageDtos.EditMessageRequest req, @Uid Long uid) {
        return view(messageService.edit(id, uid, req.body()));
    }

    private MessageDtos.MessageView view(Message m) {
        return new MessageDtos.MessageView(m.getId(), m.getChannelId(), m.getUserId(), m.getBody(),
                m.getParentId(), m.getCreatedAt(), m.getEditedAt());
    }
}
