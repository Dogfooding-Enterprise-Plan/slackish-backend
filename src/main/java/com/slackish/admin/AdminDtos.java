package com.slackish.admin;

import com.slackish.workspace.Role;

public class AdminDtos {
    public record DeactivateRequest(Long userId) {}
    public record ChangeRoleRequest(Long userId, Role role) {}
}
