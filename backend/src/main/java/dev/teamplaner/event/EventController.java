package dev.teamplaner.event;

import dev.teamplaner.event.EventDtos.AttendanceRequest;
import dev.teamplaner.event.EventDtos.CreateEventRequest;
import dev.teamplaner.event.EventDtos.EventResponse;
import dev.teamplaner.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService events;

    public EventController(EventService events) {
        this.events = events;
    }

    @PostMapping("/teams/{teamId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId,
                                @Valid @RequestBody CreateEventRequest request) {
        return events.create(CurrentUser.id(jwt), teamId, request);
    }

    @GetMapping("/teams/{teamId}/events")
    public List<EventResponse> forTeam(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId) {
        return events.listForTeam(CurrentUser.id(jwt), teamId);
    }

    /** Dashboard: kommende Termine aller eigenen Teams. */
    @GetMapping("/events")
    public List<EventResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return events.listMine(CurrentUser.id(jwt));
    }

    @GetMapping("/events/{eventId}")
    public EventResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long eventId) {
        return events.get(CurrentUser.id(jwt), eventId);
    }

    @PutMapping("/events/{eventId}/attendance")
    public EventResponse respond(@AuthenticationPrincipal Jwt jwt, @PathVariable Long eventId,
                                 @Valid @RequestBody AttendanceRequest request) {
        return events.respond(CurrentUser.id(jwt), eventId, request.status());
    }
}
