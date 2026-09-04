package com.slackish.workspace;

import com.slackish.common.Errors;
import com.slackish.common.Uid;
import com.slackish.user.User;
import com.slackish.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final MembershipRepository memberships;
    private final UserRepository users;

    public WorkspaceController(WorkspaceService workspaceService, MembershipRepository memberships, UserRepository users) {
        this.workspaceService = workspaceService;
        this.memberships = memberships;
        this.users = users;
    }

    @PostMapping
    public WorkspaceDtos.WorkspaceView create(@RequestBody WorkspaceDtos.CreateWorkspaceRequest req, @Uid Long uid) {
        Workspace w = workspaceService.create(req.name(), req.slug(), uid);
        return view(w);
    }

    @GetMapping
    public List<WorkspaceDtos.WorkspaceView> mine(@Uid Long uid) {
        return workspaceService.forUser(uid).stream().map(this::view).toList();
    }

    @GetMapping("/{id}/members")
    public List<WorkspaceDtos.MemberView> members(@PathVariable Long id, @Uid Long uid) {
        if (memberships.findByUserId(uid).isEmpty()) {
            throw Errors.forbidden("Not a member");
        }
        return memberships.findByWorkspaceId(id).stream().map(m -> {
            User u = users.findById(m.getUserId()).orElse(null);
            return new WorkspaceDtos.MemberView(
                    m.getUserId(),
                    u != null ? u.getDisplayName() : null,
                    u != null ? u.getEmail() : null,
                    m.getRole(),
                    m.isActive());
        }).toList();
    }

    @PatchMapping("/{id}/members/{userId}")
    public WorkspaceDtos.MemberView updateMember(@PathVariable Long id, @PathVariable Long userId,
                                                 @RequestBody WorkspaceDtos.UpdateMemberRequest req, @Uid Long uid) {
        Membership target = memberships.findByWorkspaceIdAndUserId(id, userId)
                .orElseThrow(() -> Errors.notFound("Member not found"));
        target.setRole(req.role());
        memberships.save(target);
        User u = users.findById(userId).orElse(null);
        return new WorkspaceDtos.MemberView(userId, u != null ? u.getDisplayName() : null,
                u != null ? u.getEmail() : null, target.getRole(), target.isActive());
    }

    private WorkspaceDtos.WorkspaceView view(Workspace w) {
        return new WorkspaceDtos.WorkspaceView(w.getId(), w.getName(), w.getSlug(), w.getOwnerId());
    }
}
