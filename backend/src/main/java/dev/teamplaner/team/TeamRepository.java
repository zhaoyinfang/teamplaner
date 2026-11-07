package dev.teamplaner.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    // Alle Teams eines Users (0, 1 oder mehrere)
    @Query("""
           select t from Team t
           where t.id in (select m.id.teamId from Membership m where m.id.userId = :userId)
           """)
    List<Team> findAllByMemberUserId(@Param("userId") Long userId);
}
