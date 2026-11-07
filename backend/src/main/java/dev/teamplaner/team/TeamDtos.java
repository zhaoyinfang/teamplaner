package dev.teamplaner.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class TeamDtos {

    private TeamDtos() {
    }

    public record CreateTeamRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record JoinTeamRequest(@NotBlank @Size(max = 16) String inviteCode) {
    }

    /** inviteCode ist nur fuer den Owner gesetzt, sonst null. */
    public record TeamResponse(Long id, String name, TeamRole role, String inviteCode) {
    }

    public record MemberResponse(Long userId, String displayName, TeamRole role) {
    }
}
