package dev.teamplaner.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select e from Event e join fetch e.team where e.id = :id")
    Optional<Event> findWithTeamById(@Param("id") Long id);

    @Query("""
           select e from Event e join fetch e.team
           where e.team.id = :teamId and e.startsAt > :after
           order by e.startsAt
           """)
    List<Event> findUpcomingByTeam(@Param("teamId") Long teamId, @Param("after") Instant after);

    // Kommende Termine aller Teams, in denen der User Mitglied ist
    @Query("""
           select e from Event e join fetch e.team
           where e.startsAt > :after
             and e.team.id in (select m.id.teamId from Membership m where m.id.userId = :userId)
           order by e.startsAt
           """)
    List<Event> findUpcomingForUser(@Param("userId") Long userId, @Param("after") Instant after);
}
