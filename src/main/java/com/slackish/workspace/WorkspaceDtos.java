package com.slackish.workspace;

public class WorkspaceDtos {
    public record CreateWorkspaceRequest(String name, String slug) {}
    public record WorkspaceView(Long id, String name, String slug, Long ownerId) {}
    public record MemberView(Long userId, String displayName, String email, Role role, boolean active) {}
    public record UpdateMemberRequest(Role role) {}
}
