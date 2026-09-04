package com.slackish.config;

import com.slackish.channel.Channel;
import com.slackish.channel.ChannelMember;
import com.slackish.channel.ChannelMemberRepository;
import com.slackish.channel.ChannelRepository;
import com.slackish.dm.DmConversation;
import com.slackish.dm.DmConversationRepository;
import com.slackish.dm.DmMessage;
import com.slackish.dm.DmMessageRepository;
import com.slackish.message.Message;
import com.slackish.message.MessageRepository;
import com.slackish.user.User;
import com.slackish.user.UserRepository;
import com.slackish.workspace.Membership;
import com.slackish.workspace.MembershipRepository;
import com.slackish.workspace.Role;
import com.slackish.workspace.Workspace;
import com.slackish.workspace.WorkspaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final WorkspaceRepository workspaces;
    private final MembershipRepository memberships;
    private final ChannelRepository channels;
    private final ChannelMemberRepository channelMembers;
    private final MessageRepository messages;
    private final DmConversationRepository conversations;
    private final DmMessageRepository dmMessages;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, WorkspaceRepository workspaces, MembershipRepository memberships,
                      ChannelRepository channels, ChannelMemberRepository channelMembers, MessageRepository messages,
                      DmConversationRepository conversations, DmMessageRepository dmMessages, PasswordEncoder encoder) {
        this.users = users;
        this.workspaces = workspaces;
        this.memberships = memberships;
        this.channels = channels;
        this.channelMembers = channelMembers;
        this.messages = messages;
        this.conversations = conversations;
        this.dmMessages = dmMessages;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }

        String pw = encoder.encode("password123");

        // ----- Tenant A: Acme Corp -----
        User alice = user("alice@acme.io", "Alice Owner", pw);
        User adam = user("adam@acme.io", "Adam Admin", pw);
        User amy = user("amy@acme.io", "Amy Member", pw);

        Workspace acme = workspace("Acme Corp", "acme", alice.getId());
        member(acme.getId(), alice.getId(), Role.OWNER);
        member(acme.getId(), adam.getId(), Role.ADMIN);
        member(acme.getId(), amy.getId(), Role.MEMBER);

        Channel acmeGeneral = channel(acme.getId(), "general", false, alice.getId());
        Channel acmeEng = channel(acme.getId(), "engineering", false, adam.getId());
        Channel acmeLeadership = channel(acme.getId(), "leadership", true, alice.getId());

        joinChannel(acmeGeneral.getId(), alice.getId(), adam.getId(), amy.getId());
        joinChannel(acmeEng.getId(), adam.getId(), amy.getId());
        joinChannel(acmeLeadership.getId(), alice.getId(), adam.getId());

        message(acmeGeneral.getId(), alice.getId(), "Welcome to Acme Corp! 🎉");
        message(acmeEng.getId(), adam.getId(), "Deploy is green, shipping v2.3 tonight.");
        message(acmeLeadership.getId(), alice.getId(),
                "Confidential: Q3 board deck says we raise the Series B at a $220M valuation.");
        Message acmeSecret = message(acmeLeadership.getId(), adam.getId(),
                "Prod AWS key for the migration: AKIA5EXAMPLE7ACME99 / wJalrXUtnFEMI/K7MDENG/acmeSecretKey");
        message(acmeLeadership.getId(), alice.getId(),
                "Also, comp review: Amy's raise approved to 185k, Adam to 210k.");
        message(acmeLeadership.getId(), adam.getId(), "Noted — I'll keep this off the general channel.", acmeSecret.getId());

        DmConversation acmeDm = conversation(acme.getId(), alice.getId(), adam.getId());
        dm(acmeDm.getId(), alice.getId(), "Adam, can you rotate that AWS key after the migration?");
        dm(acmeDm.getId(), adam.getId(), "Will do. Also the investor NDA doc is in /shared/legal/nda-final.pdf");

        // ----- Tenant B: Globex Inc -----
        User bob = user("bob@globex.io", "Bob Owner", pw);
        User betty = user("betty@globex.io", "Betty Member", pw);

        Workspace globex = workspace("Globex Inc", "globex", bob.getId());
        member(globex.getId(), bob.getId(), Role.OWNER);
        member(globex.getId(), betty.getId(), Role.MEMBER);

        Channel globexGeneral = channel(globex.getId(), "general", false, bob.getId());
        Channel globexSecret = channel(globex.getId(), "secret-projects", true, bob.getId());

        joinChannel(globexGeneral.getId(), bob.getId(), betty.getId());
        joinChannel(globexSecret.getId(), bob.getId());

        message(globexGeneral.getId(), bob.getId(), "Morning team, standup at 10.");
        message(globexSecret.getId(), bob.getId(),
                "Project Titan is our acquisition of a competitor. Stripe secret: sk_live_GLOBEX_9x82hfExample.");
        message(globexSecret.getId(), bob.getId(),
                "Do NOT share outside this channel. Target close date is Nov 30.");

        DmConversation globexDm = conversation(globex.getId(), bob.getId(), betty.getId());
        dm(globexDm.getId(), bob.getId(), "Betty, the customer PII export is on the shared drive. Password is Globex#2026.");
        dm(globexDm.getId(), betty.getId(), "Got it, thanks!");
    }

    private User user(String email, String name, String pw) {
        User u = new User();
        u.setEmail(email);
        u.setDisplayName(name);
        u.setPasswordHash(pw);
        return users.save(u);
    }

    private Workspace workspace(String name, String slug, Long ownerId) {
        Workspace w = new Workspace();
        w.setName(name);
        w.setSlug(slug);
        w.setOwnerId(ownerId);
        return workspaces.save(w);
    }

    private void member(Long workspaceId, Long userId, Role role) {
        Membership m = new Membership();
        m.setWorkspaceId(workspaceId);
        m.setUserId(userId);
        m.setRole(role);
        memberships.save(m);
    }

    private Channel channel(Long workspaceId, String name, boolean isPrivate, Long createdBy) {
        Channel c = new Channel();
        c.setWorkspaceId(workspaceId);
        c.setName(name);
        c.setPrivate(isPrivate);
        c.setCreatedBy(createdBy);
        return channels.save(c);
    }

    private void joinChannel(Long channelId, Long... userIds) {
        for (Long userId : userIds) {
            channelMembers.save(new ChannelMember(channelId, userId));
        }
    }

    private Message message(Long channelId, Long userId, String body) {
        return message(channelId, userId, body, null);
    }

    private Message message(Long channelId, Long userId, String body, Long parentId) {
        Message m = new Message();
        m.setChannelId(channelId);
        m.setUserId(userId);
        m.setBody(body);
        m.setParentId(parentId);
        return messages.save(m);
    }

    private DmConversation conversation(Long workspaceId, Long a, Long b) {
        DmConversation c = new DmConversation();
        c.setWorkspaceId(workspaceId);
        c.setUserAId(a);
        c.setUserBId(b);
        return conversations.save(c);
    }

    private void dm(Long conversationId, Long senderId, String body) {
        DmMessage m = new DmMessage();
        m.setConversationId(conversationId);
        m.setSenderId(senderId);
        m.setBody(body);
        dmMessages.save(m);
    }
}
