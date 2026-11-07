package dev.teamplaner.event;

import dev.teamplaner.attendance.Attendance;
import dev.teamplaner.attendance.AttendanceId;
import dev.teamplaner.attendance.AttendanceRepository;
import dev.teamplaner.attendance.AttendanceStatus;
import dev.teamplaner.common.ApiException;
import dev.teamplaner.event.EventDtos.AttendanceCounts;
import dev.teamplaner.event.EventDtos.CreateEventRequest;
import dev.teamplaner.event.EventDtos.EventResponse;
import dev.teamplaner.team.TeamAccessService;
import dev.teamplaner.team.TeamRepository;
import dev.teamplaner.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private final EventRepository events;
    private final AttendanceRepository attendances;
    private final TeamAccessService access;
    private final TeamRepository teams;
    private final UserRepository users;

    public EventService(EventRepository events, AttendanceRepository attendances,
                        TeamAccessService access, TeamRepository teams, UserRepository users) {
        this.events = events;
        this.attendances = attendances;
        this.access = access;
        this.teams = teams;
        this.users = users;
    }

    /** Nur der Owner darf Termine veroeffentlichen. */
    @Transactional
    public EventResponse create(Long userId, Long teamId, CreateEventRequest req) {
        access.requireOwner(userId, teamId);

        Instant now = Instant.now();
        if (!req.startsAt().isAfter(now)) {
            throw ApiException.badRequest("startsAt must be in the future");
        }
        if (req.endsAt() != null && !req.endsAt().isAfter(req.startsAt())) {
            throw ApiException.badRequest("endsAt must be after startsAt");
        }
        if (req.respondUntil() != null && req.respondUntil().isAfter(req.startsAt())) {
            throw ApiException.badRequest("respondUntil must not be after startsAt");
        }

        Event event = new Event();
        event.setTeam(teams.getReferenceById(teamId));
        event.setCreatedBy(users.getReferenceById(userId));
        event.setTitle(req.title().trim());
        event.setType(req.type());
        event.setLocation(req.location());
        event.setMeetAt(req.meetAt());
        event.setStartsAt(req.startsAt());
        event.setEndsAt(req.endsAt());
        event.setRespondUntil(req.respondUntil());
        events.save(event);

        return toResponse(event, userId);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listForTeam(Long userId, Long teamId) {
        access.requireMember(userId, teamId);
        return toResponses(events.findUpcomingByTeam(teamId, Instant.now()), userId);
    }

    /** "Naechste Termine" im Dashboard: alle Teams des Users. */
    @Transactional(readOnly = true)
    public List<EventResponse> listMine(Long userId) {
        return toResponses(events.findUpcomingForUser(userId, Instant.now()), userId);
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long userId, Long eventId) {
        Event event = load(eventId);
        access.requireMember(userId, event.getTeam().getId());
        return toResponse(event, userId);
    }

    /** Zusage, Absage oder "Vielleicht". Wiederholtes Antworten ueberschreibt die letzte Antwort. */
    @Transactional
    public EventResponse respond(Long userId, Long eventId, AttendanceStatus status) {
        Event event = load(eventId);
        access.requireMember(userId, event.getTeam().getId());

        Instant now = Instant.now();
        if (!event.isResponseOpen(now)) {
            throw ApiException.conflict("Responses for this event are closed");
        }

        Attendance attendance = attendances.findById(new AttendanceId(eventId, userId))
                .orElseGet(() -> new Attendance(eventId, userId, status));
        attendance.setStatus(status);
        attendance.setUpdatedAt(now);
        attendances.save(attendance);

        return toResponse(event, userId);
    }

    private Event load(Long eventId) {
        return events.findWithTeamById(eventId)
                .orElseThrow(() -> ApiException.notFound("Event not found"));
    }

    private EventResponse toResponse(Event event, Long userId) {
        return toResponses(List.of(event), userId).get(0);
    }

    /** Zaehler und eigener Status fuer alle Termine mit je einer Abfrage (kein N+1). */
    private List<EventResponse> toResponses(List<Event> list, Long userId) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = list.stream().map(Event::getId).toList();

        Map<Long, Map<AttendanceStatus, Long>> counts = new HashMap<>();
        for (Object[] row : attendances.countByEventIdsAndStatus(ids)) {
            Long eventId = (Long) row[0];
            AttendanceStatus status = (AttendanceStatus) row[1];
            Long count = (Long) row[2];
            counts.computeIfAbsent(eventId, k -> new EnumMap<>(AttendanceStatus.class)).put(status, count);
        }

        Map<Long, AttendanceStatus> mine = new HashMap<>();
        for (Attendance a : attendances.findByUserAndEvents(userId, ids)) {
            mine.put(a.getId().getEventId(), a.getStatus());
        }

        Instant now = Instant.now();
        return list.stream().map(e -> {
            Map<AttendanceStatus, Long> c = counts.getOrDefault(e.getId(), Map.of());
            return new EventResponse(
                    e.getId(),
                    e.getTeam().getId(),
                    e.getTeam().getName(),
                    e.getTitle(),
                    e.getType(),
                    e.getLocation(),
                    e.getMeetAt(),
                    e.getStartsAt(),
                    e.getEndsAt(),
                    e.getRespondUntil(),
                    e.isResponseOpen(now),
                    new AttendanceCounts(
                            c.getOrDefault(AttendanceStatus.ACCEPTED, 0L),
                            c.getOrDefault(AttendanceStatus.MAYBE, 0L),
                            c.getOrDefault(AttendanceStatus.DECLINED, 0L)),
                    mine.get(e.getId()));
        }).toList();
    }
}
