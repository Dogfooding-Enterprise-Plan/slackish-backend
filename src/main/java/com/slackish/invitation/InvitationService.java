package com.slackish.invitation;

import com.slackish.common.Errors;
import com.slackish.user.User;
import com.slackish.user.UserRepository;
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
    private final UserRepository users;

    public InvitationService(InvitationRepository invitations, MembershipRepository memberships,
                             WorkspaceService workspaceService, UserRepository users) {
        this.invitations = invitations;
        this.memberships = memberships;
        this.workspaceService = workspaceService;
        this.users = users;
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
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw Errors.badRequest("Invitation is no longer valid");
        }
        if (inv.getExpiresAt() == null || !inv.getExpiresAt().isAfter(Instant.now())) {
            throw Errors.badRequest("Invitation has expired");
        }
        User user = users.findById(userId)
                .orElseThrow(() -> Errors.unauthorized("Authentication required"));
        if (user.getEmail() == null || inv.getEmail() == null
                || !user.getEmail().trim().equalsIgnoreCase(inv.getEmail().trim())) {
            throw Errors.forbidden("This invitation was issued to a different email address");
        }
        Membership m = memberships.findByWorkspaceIdAndUserId(inv.getWorkspaceId(), userId)
                .orElseGet(Membership::new);
        m.setWorkspaceId(inv.getWorkspaceId());
        m.setUserId(userId);
        m.setRole(inv.getRole());
        m.setActive(true);
        Membership saved = memberships.save(m);
        inv.setStatus(InvitationStatus.ACCEPTED);
        invitations.save(inv);
        return saved;
    }
}
