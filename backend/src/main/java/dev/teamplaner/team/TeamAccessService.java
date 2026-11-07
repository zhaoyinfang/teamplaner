package dev.teamplaner.team;

import dev.teamplaner.common.ApiException;
import org.springframework.stereotype.Service;

/** Zentrale Zugriffspruefung: Nur Teammitglieder sehen ein Team, nur der Owner darf verwalten. */
@Service
public class TeamAccessService {

    private final MembershipRepository memberships;

    public TeamAccessService(MembershipRepository memberships) {
        this.memberships = memberships;
    }

    public void requireMember(Long userId, Long teamId) {
        if (!memberships.existsByIdUserIdAndIdTeamId(userId, teamId)) {
            throw ApiException.forbidden("You are not a member of this team");
        }
    }

    public void requireOwner(Long userId, Long teamId) {
        if (!memberships.existsByIdUserIdAndIdTeamIdAndRole(userId, teamId, TeamRole.OWNER)) {
            throw ApiException.forbidden("Only the team owner can do this");
        }
    }
}
