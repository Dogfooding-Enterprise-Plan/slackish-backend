package com.slackish.admin;

import com.slackish.audit.AuditLog;
import com.slackish.audit.AuditLogRepository;
import com.slackish.audit.AuditService;
import com.slackish.channel.Channel;
import com.slackish.channel.ChannelRepository;
import com.slackish.common.Errors;
import com.slackish.common.Uid;
import com.slackish.dm.DmConversationRepository;
import com.slackish.invitation.InvitationRepository;
import com.slackish.message.Message;
import com.slackish.message.MessageRepository;
import com.slackish.user.UserRepository;
import com.slackish.workspace.Membership;
import com.slackish.workspace.MembershipRepository;
import com.slackish.workspace.WorkspaceRepository;
import com.slackish.workspace.WorkspaceService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WorkspaceService workspaceService;
    private final MembershipRepository memberships;
    private final UserRepository users;
    private final ChannelRepository channels;
    private final MessageRepository messages;
    private final DmConversationRepository conversations;
    private final InvitationRepository invitations;
    private final WorkspaceRepository workspaces;
    private final AuditLogRepository auditLogs;
    private final AuditService auditService;

    public AdminController(WorkspaceService workspaceService, MembershipRepository memberships,
                           UserRepository users, ChannelRepository channels, MessageRepository messages,
                           DmConversationRepository conversations, InvitationRepository invitations,
                           WorkspaceRepository workspaces, AuditLogRepository auditLogs, AuditService auditService) {
        this.workspaceService = workspaceService;
        this.memberships = memberships;
        this.users = users;
        this.channels = channels;
        this.messages = messages;
        this.conversations = conversations;
        this.invitations = invitations;
        this.workspaces = workspaces;
        this.auditLogs = auditLogs;
        this.auditService = auditService;
    }

    @PostMapping("/workspaces/{id}/deactivate-member")
    public void deactivate(@PathVariable Long id, @RequestBody AdminDtos.DeactivateRequest req, @Uid Long uid) {
        workspaceService.requireAdmin(id, uid);
        Membership m = memberships.findByWorkspaceIdAndUserId(id, req.userId())
                .orElseThrow(() -> Errors.notFound("Member not found"));
        m.setActive(false);
        memberships.save(m);
        auditService.record(id, uid, "DEACTIVATE_MEMBER", String.valueOf(req.userId()));
    }

    @PostMapping("/workspaces/{id}/change-role")
    public void changeRole(@PathVariable Long id, @RequestBody AdminDtos.ChangeRoleRequest req, @Uid Long uid) {
        workspaceService.requireAdmin(id, uid);
        Membership m = memberships.findByWorkspaceIdAndUserId(id, req.userId())
                .orElseThrow(() -> Errors.notFound("Member not found"));
        m.setRole(req.role());
        memberships.save(m);
        auditService.record(id, uid, "CHANGE_ROLE", String.valueOf(req.userId()));
    }

    @GetMapping("/workspaces/{id}/audit")
    public List<AuditLog> audit(@PathVariable Long id, @Uid Long uid) {
        workspaceService.requireAdmin(id, uid);
        return auditLogs.findByWorkspaceIdOrderByCreatedAtDesc(id);
    }

    @GetMapping("/workspaces/{id}/export")
    public Map<String, Object> export(@PathVariable Long id) {
        Map<String, Object> dump = new LinkedHashMap<>();
        dump.put("workspace", workspaces.findById(id).orElse(null));
        List<Membership> ms = memberships.findByWorkspaceId(id);
        dump.put("memberships", ms);
        dump.put("members", ms.stream().map(m -> users.findById(m.getUserId()).orElse(null)).toList());
        List<Channel> chans = channels.findByWorkspaceId(id);
        dump.put("channels", chans);
        List<Message> msgs = new ArrayList<>();
        for (Channel c : chans) {
            msgs.addAll(messages.findByChannelId(c.getId()));
        }
        dump.put("messages", msgs);
        dump.put("conversations", conversations.findByWorkspaceId(id));
        dump.put("invitations", invitations.findByWorkspaceId(id));
        return dump;
    }
}
