package dev.teamplaner.team;

import dev.teamplaner.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** n:m-Verbindung zwischen User und Team, mit Rolle. Zusammengesetzter Primaerschluessel. */
@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
public class Membership {

    @EmbeddedId
    private MembershipId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRole role;

    @Column(name = "joined_at", nullable = false, insertable = false, updatable = false)
    private Instant joinedAt;

    public Membership(User user, Team team, TeamRole role) {
        this.id = new MembershipId(user.getId(), team.getId());
        this.user = user;
        this.team = team;
        this.role = role;
    }
}
