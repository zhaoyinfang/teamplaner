package dev.teamplaner.event;

import dev.teamplaner.attendance.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class EventDtos {

    private EventDtos() {
    }

    public record CreateEventRequest(
            @NotBlank @Size(max = 150) String title,
            @NotNull EventType type,
            @Size(max = 255) String location,
            Instant meetAt,
            @NotNull Instant startsAt,
            Instant endsAt,
            Instant respondUntil) {
    }

    public record AttendanceRequest(@NotNull AttendanceStatus status) {
    }

    public record AttendanceCounts(long accepted, long maybe, long declined) {
    }

    /** myStatus ist null, solange der User noch nicht geantwortet hat. */
    public record EventResponse(
            Long id,
            Long teamId,
            String teamName,
            String title,
            EventType type,
            String location,
            Instant meetAt,
            Instant startsAt,
            Instant endsAt,
            Instant respondUntil,
            boolean responseOpen,
            AttendanceCounts counts,
            AttendanceStatus myStatus) {
    }
}
