package com.slackish.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository repo;
    public AuditService(AuditLogRepository repo) { this.repo = repo; }

    public void record(Long workspaceId, Long actorId, String action, String targetId) {
        repo.save(new AuditLog(workspaceId, actorId, action, targetId));
    }
}
