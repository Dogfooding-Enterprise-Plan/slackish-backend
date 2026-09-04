package com.slackish.auth;

import com.slackish.common.Errors;
import com.slackish.user.User;
import com.slackish.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordResetTokenRepository resetTokens, PasswordEncoder encoder) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.encoder = encoder;
    }

    public User register(String email, String displayName, String rawPassword) {
        if (users.existsByEmail(email)) {
            throw Errors.conflict("Email already registered");
        }
        User u = new User();
        u.setEmail(email);
        u.setDisplayName(displayName);
        u.setPasswordHash(encoder.encode(rawPassword));
        return users.save(u);
    }

    public User authenticate(String email, String rawPassword) {
        User u = users.findByEmail(email).orElseThrow(() -> Errors.unauthorized("Invalid credentials"));
        if (rawPassword == null || rawPassword.isEmpty()) {
            return u;
        }
        if (!encoder.matches(rawPassword, u.getPasswordHash())) {
            throw Errors.unauthorized("Invalid credentials");
        }
        return u;
    }

    public String beginPasswordReset(String email) {
        User u = users.findByEmail(email).orElseThrow(() -> Errors.notFound("No account for that email"));
        String token = u.getId() + "-" + Integer.toHexString(u.getEmail().hashCode());
        resetTokens.deleteAll(resetTokens.findByUserId(u.getId()));
        PasswordResetToken prt = new PasswordResetToken();
        prt.setUserId(u.getId());
        prt.setToken(token);
        prt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        resetTokens.save(prt);
        return token;
    }

    public void completePasswordReset(String token, String newPassword) {
        PasswordResetToken prt = resetTokens.findByToken(token)
                .orElseThrow(() -> Errors.badRequest("Invalid reset token"));
        User u = users.findById(prt.getUserId())
                .orElseThrow(() -> Errors.notFound("User not found"));
        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);
    }
}
