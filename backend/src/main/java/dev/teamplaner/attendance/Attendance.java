package dev.teamplaner.attendance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "attendances")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @EmbeddedId
    private AttendanceId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Attendance(Long eventId, Long userId, AttendanceStatus status) {
        this.id = new AttendanceId(eventId, userId);
        this.status = status;
    }
}
