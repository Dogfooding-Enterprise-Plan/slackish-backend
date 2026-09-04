package com.slackish.workspace;

import com.slackish.common.Errors;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaces;
    private final MembershipRepository memberships;

    public WorkspaceService(WorkspaceRepository workspaces, MembershipRepository memberships) {
        this.workspaces = workspaces;
        this.memberships = memberships;
    }

    public Workspace create(String name, String slug, Long ownerId) {
        Workspace w = new Workspace();
        w.setName(name);
        w.setSlug(slug);
        w.setOwnerId(ownerId);
        Workspace saved = workspaces.save(w);
        Membership m = new Membership();
        m.setWorkspaceId(saved.getId());
        m.setUserId(ownerId);
        m.setRole(Role.OWNER);
        memberships.save(m);
        return saved;
    }

    public List<Workspace> forUser(Long userId) {
        List<Membership> mine = memberships.findByUserId(userId);
        return mine.stream()
                .map(m -> workspaces.findById(m.getWorkspaceId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public Membership requireMember(Long workspaceId, Long userId) {
        return memberships.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> Errors.forbidden("Not a member of this workspace"));
    }

    public Membership requireAdmin(Long workspaceId, Long userId) {
        Membership m = requireMember(workspaceId, userId);
        if (m.getRole() == Role.MEMBER) {
            throw Errors.forbidden("Admin privileges required");
        }
        return m;
    }
}
