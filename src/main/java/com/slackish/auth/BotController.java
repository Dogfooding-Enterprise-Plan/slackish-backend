package com.slackish.auth;

import com.slackish.common.Uid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    private final JwtService jwtService;

    public BotController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public AuthDtos.TokenResponse createToken(@Uid Long uid) {
        return new AuthDtos.TokenResponse(jwtService.issue(uid));
    }
}
