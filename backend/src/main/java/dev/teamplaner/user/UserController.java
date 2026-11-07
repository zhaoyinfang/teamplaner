package dev.teamplaner.user;

import dev.teamplaner.common.ApiException;
import dev.teamplaner.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public record UserResponse(Long id, String email, String displayName) {
    }

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        User user = users.findById(CurrentUser.id(jwt))
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
