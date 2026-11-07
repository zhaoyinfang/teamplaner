package dev.teamplaner.team;

import dev.teamplaner.security.CurrentUser;
import dev.teamplaner.team.TeamDtos.CreateTeamRequest;
import dev.teamplaner.team.TeamDtos.JoinTeamRequest;
import dev.teamplaner.team.TeamDtos.MemberResponse;
import dev.teamplaner.team.TeamDtos.TeamResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teams;

    public TeamController(TeamService teams) {
        this.teams = teams;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateTeamRequest request) {
        return teams.create(CurrentUser.id(jwt), request.name());
    }

    @PostMapping("/join")
    public TeamResponse join(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody JoinTeamRequest request) {
        return teams.join(CurrentUser.id(jwt), request.inviteCode());
    }

    @GetMapping
    public List<TeamResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return teams.listMine(CurrentUser.id(jwt));
    }

    @GetMapping("/{teamId}")
    public TeamResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId) {
        return teams.get(CurrentUser.id(jwt), teamId);
    }

    @GetMapping("/{teamId}/members")
    public List<MemberResponse> members(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId) {
        return teams.members(CurrentUser.id(jwt), teamId);
    }
}
