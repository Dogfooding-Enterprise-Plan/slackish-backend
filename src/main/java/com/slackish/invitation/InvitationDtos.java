package com.slackish.invitation;

import com.slackish.workspace.Role;
import java.time.Instant;
import java.util.List;

public class InvitationDtos {
    public record InviteRequest(String email, Role role) {}
    public record BulkInviteRequest(List<String> emails, Role role) {}
    public record AcceptRequest(String token) {}
    public record InvitationView(Long id, Long workspaceId, String email, String token,
                                 Role role, InvitationStatus status, Instant expiresAt) {}
    public record MembershipView(Long workspaceId, Long userId, Role role, boolean active) {}
}
