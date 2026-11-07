package dev.teamplaner.team;

import dev.teamplaner.common.ApiException;
import dev.teamplaner.team.TeamDtos.MemberResponse;
import dev.teamplaner.team.TeamDtos.TeamResponse;
import dev.teamplaner.user.User;
import dev.teamplaner.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

@Service
public class TeamService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // ohne O/0/I/1
    private static final int CODE_LENGTH = 8;

    private final TeamRepository teams;
    private final MembershipRepository memberships;
    private final UserRepository users;
    private final TeamAccessService access;
    private final SecureRandom random = new SecureRandom();

    public TeamService(TeamRepository teams, MembershipRepository memberships,
                       UserRepository users, TeamAccessService access) {
        this.teams = teams;
        this.memberships = memberships;
        this.users = users;
        this.access = access;
    }

    /** Wer ein Team anlegt, wird automatisch OWNER. */
    @Transactional
    public TeamResponse create(Long userId, String name) {
        User owner = users.getReferenceById(userId);

        Team team = new Team();
        team.setName(name.trim());
        team.setInviteCode(newInviteCode());
        teams.save(team);

        memberships.save(new Membership(owner, team, TeamRole.OWNER));
        return new TeamResponse(team.getId(), team.getName(), TeamRole.OWNER, team.getInviteCode());
    }

    @Transactional
    public TeamResponse join(Long userId, String inviteCode) {
        Team team = teams.findByInviteCode(inviteCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> ApiException.notFound("Invalid invite code"));
        if (memberships.existsByIdUserIdAndIdTeamId(userId, team.getId())) {
            throw ApiException.conflict("You are already a member of this team");
        }
        memberships.save(new Membership(users.getReferenceById(userId), team, TeamRole.MEMBER));
        return toResponse(team, TeamRole.MEMBER);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> listMine(Long userId) {
        return memberships.findAllByUserIdWithTeam(userId).stream()
                .map(m -> toResponse(m.getTeam(), m.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse get(Long userId, Long teamId) {
        Membership m = memberships.findByUserIdAndTeamIdWithTeam(userId, teamId)
                .orElseThrow(() -> ApiException.forbidden("You are not a member of this team"));
        return toResponse(m.getTeam(), m.getRole());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> members(Long userId, Long teamId) {
        access.requireMember(userId, teamId);
        return memberships.findAllByTeamIdWithUser(teamId).stream()
                .map(m -> new MemberResponse(m.getUser().getId(), m.getUser().getDisplayName(), m.getRole()))
                .toList();
    }

    private TeamResponse toResponse(Team team, TeamRole role) {
        String code = role == TeamRole.OWNER ? team.getInviteCode() : null;
        return new TeamResponse(team.getId(), team.getName(), role, code);
    }

    private String newInviteCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (teams.existsByInviteCode(code));
        return code;
    }
}
