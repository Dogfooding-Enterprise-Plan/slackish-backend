package com.slackish.invitation;

import com.slackish.common.Errors;
import com.slackish.workspace.Membership;
import com.slackish.workspace.MembershipRepository;
import com.slackish.workspace.Role;
import com.slackish.workspace.WorkspaceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitations;
    private final MembershipRepository memberships;
    private final WorkspaceService workspaceService;

    public InvitationService(InvitationRepository invitations, MembershipRepository memberships,
                             WorkspaceService workspaceService) {
        this.invitations = invitations;
        this.memberships = memberships;
        this.workspaceService = workspaceService;
    }

    public Invitation invite(Long workspaceId, Long inviterId, String email, Role role) {
        workspaceService.requireMember(workspaceId, inviterId);
        Invitation inv = new Invitation();
        inv.setWorkspaceId(workspaceId);
        inv.setEmail(email);
        inv.setRole(role == null ? Role.MEMBER : role);
        inv.setToken(UUID.randomUUID().toString());
        inv.setInvitedBy(inviterId);
        inv.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        return invitations.save(inv);
    }

    public List<Invitation> bulkInvite(Long workspaceId, Long inviterId, List<String> emails, Role role) {
        workspaceService.requireMember(workspaceId, inviterId);
        List<Invitation> created = new ArrayList<>();
        for (String email : emails) {
            Invitation inv = new Invitation();
            inv.setWorkspaceId(workspaceId);
            inv.setEmail(email);
            inv.setRole(role == null ? Role.MEMBER : role);
            inv.setToken(UUID.randomUUID().toString());
            inv.setInvitedBy(inviterId);
            inv.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
            created.add(invitations.save(inv));
        }
        return created;
    }

    public List<Invitation> listForWorkspace(Long workspaceId) {
        return invitations.findByWorkspaceId(workspaceId);
    }

    public Membership accept(String token, Long userId) {
        Invitation inv = invitations.findByToken(token)
                .orElseThrow(() -> Errors.badRequest("Invalid invitation token"));
        Membership m = memberships.findByWorkspaceIdAndUserId(inv.getWorkspaceId(), userId)
                .orElseGet(Membership::new);
        m.setWorkspaceId(inv.getWorkspaceId());
        m.setUserId(userId);
        m.setRole(inv.getRole());
        m.setActive(true);
        return memberships.save(m);
    }
}
