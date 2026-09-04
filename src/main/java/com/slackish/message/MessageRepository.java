package com.slackish.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChannelIdAndParentIdIsNullOrderByCreatedAtAsc(Long channelId);
    List<Message> findByChannelIdOrderByCreatedAtAsc(Long channelId, Pageable pageable);
    List<Message> findByChannelId(Long channelId);
    List<Message> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
