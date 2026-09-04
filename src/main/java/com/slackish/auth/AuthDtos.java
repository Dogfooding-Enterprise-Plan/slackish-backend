package com.slackish.auth;

public class AuthDtos {

    public record RegisterRequest(String email, String displayName, String password) {}
    public record LoginRequest(String email, String password) {}
    public record ForgotRequest(String email) {}
    public record ResetRequest(String token, String newPassword) {}

    public record UserView(Long id, String email, String displayName, boolean mfaEnabled) {}

    public record ForgotResponse(String message, String resetToken) {}
    public record TokenResponse(String token) {}
}
