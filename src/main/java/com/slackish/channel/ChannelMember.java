package com.slackish.channel;

import jakarta.persistence.*;

@Entity
@Table(name = "channel_members")
public class ChannelMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelId;

    @Column(nullable = false)
    private Long userId;

    public ChannelMember() {}
    public ChannelMember(Long channelId, Long userId) {
        this.channelId = channelId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
