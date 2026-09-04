package com.slackish.auth;

import com.slackish.common.CurrentUserResolver;
import com.slackish.common.Errors;
import com.slackish.common.Uid;
import com.slackish.user.User;
import com.slackish.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository users;

    public AuthController(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    @PostMapping("/register")
    public AuthDtos.UserView register(@RequestBody AuthDtos.RegisterRequest req, HttpServletRequest request) {
        User u = authService.register(req.email(), req.displayName(), req.password());
        HttpSession session = request.getSession(true);
        session.setAttribute(CurrentUserResolver.SESSION_UID, u.getId());
        return view(u);
    }

    @PostMapping("/login")
    public AuthDtos.UserView login(@RequestBody AuthDtos.LoginRequest req, HttpServletRequest request) {
        User u = authService.authenticate(req.email(), req.password());
        HttpSession session = request.getSession(true);
        session.setAttribute(CurrentUserResolver.SESSION_UID, u.getId());
        return view(u);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/me")
    public AuthDtos.UserView me(@Uid Long uid) {
        User u = users.findById(uid).orElseThrow(() -> Errors.notFound("User not found"));
        return view(u);
    }

    @PostMapping("/forgot")
    public AuthDtos.ForgotResponse forgot(@RequestBody AuthDtos.ForgotRequest req) {
        String token = authService.beginPasswordReset(req.email());
        return new AuthDtos.ForgotResponse("Password reset initiated", token);
    }

    @PostMapping("/reset")
    public void reset(@RequestBody AuthDtos.ResetRequest req) {
        authService.completePasswordReset(req.token(), req.newPassword());
    }

    private AuthDtos.UserView view(User u) {
        return new AuthDtos.UserView(u.getId(), u.getEmail(), u.getDisplayName(), u.isMfaEnabled());
    }
}
