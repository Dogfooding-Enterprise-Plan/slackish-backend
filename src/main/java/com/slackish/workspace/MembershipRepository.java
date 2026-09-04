package com.slackish.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserId(Long userId);
    List<Membership> findByWorkspaceId(Long workspaceId);
    Optional<Membership> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);
}
