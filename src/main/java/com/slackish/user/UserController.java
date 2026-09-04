package com.slackish.user;

import com.slackish.common.Errors;
import com.slackish.common.Uid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id, @Uid Long uid) {
        return users.findById(id).orElseThrow(() -> Errors.notFound("User not found"));
    }
}
