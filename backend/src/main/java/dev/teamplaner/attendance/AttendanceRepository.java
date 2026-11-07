package dev.teamplaner.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {

    // Zaehler pro Termin und Status fuer mehrere Termine auf einmal (eine Abfrage statt N)
    @Query("""
           select a.id.eventId, a.status, count(a)
           from Attendance a
           where a.id.eventId in :eventIds
           group by a.id.eventId, a.status
           """)
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") Collection<Long> eventIds);

    @Query("select a from Attendance a where a.id.userId = :userId and a.id.eventId in :eventIds")
    List<Attendance> findByUserAndEvents(@Param("userId") Long userId,
                                         @Param("eventIds") Collection<Long> eventIds);
}
