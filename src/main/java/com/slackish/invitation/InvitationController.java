package com.slackish.invitation;

import com.slackish.common.Uid;
import com.slackish.workspace.Membership;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/workspaces/{workspaceId}/invitations")
    public InvitationDtos.InvitationView invite(@PathVariable Long workspaceId,
                                                @RequestBody InvitationDtos.InviteRequest req, @Uid Long uid) {
        return view(invitationService.invite(workspaceId, uid, req.email(), req.role()));
    }

    @PostMapping("/workspaces/{workspaceId}/invitations/bulk")
    public List<InvitationDtos.InvitationView> bulk(@PathVariable Long workspaceId,
                                                    @RequestBody InvitationDtos.BulkInviteRequest req, @Uid Long uid) {
        return invitationService.bulkInvite(workspaceId, uid, req.emails(), req.role())
                .stream().map(this::view).toList();
    }

    @GetMapping("/workspaces/{workspaceId}/invitations")
    public List<InvitationDtos.InvitationView> list(@PathVariable Long workspaceId, @Uid Long uid) {
        return invitationService.listForWorkspace(workspaceId).stream().map(this::view).toList();
    }

    @PostMapping("/invitations/accept")
    public InvitationDtos.MembershipView accept(@RequestBody InvitationDtos.AcceptRequest req, @Uid Long uid) {
        Membership m = invitationService.accept(req.token(), uid);
        return new InvitationDtos.MembershipView(m.getWorkspaceId(), m.getUserId(), m.getRole(), m.isActive());
    }

    private InvitationDtos.InvitationView view(Invitation inv) {
        return new InvitationDtos.InvitationView(inv.getId(), inv.getWorkspaceId(), inv.getEmail(),
                inv.getToken(), inv.getRole(), inv.getStatus(), inv.getExpiresAt());
    }
}
