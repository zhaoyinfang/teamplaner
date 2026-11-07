package dev.teamplaner.event;

import dev.teamplaner.team.Team;
import dev.teamplaner.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    private String location;

    @Column(name = "meet_at")
    private Instant meetAt;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "respond_until")
    private Instant respondUntil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** Zu-/Absagen sind moeglich, solange der Termin nicht begonnen hat und die Frist nicht abgelaufen ist. */
    public boolean isResponseOpen(Instant now) {
        return startsAt.isAfter(now) && (respondUntil == null || respondUntil.isAfter(now));
    }
}
