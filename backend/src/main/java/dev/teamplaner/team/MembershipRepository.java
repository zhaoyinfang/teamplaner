package dev.teamplaner.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, MembershipId> {

    boolean existsByIdUserIdAndIdTeamId(Long userId, Long teamId);

    boolean existsByIdUserIdAndIdTeamIdAndRole(Long userId, Long teamId, TeamRole role);

    // JOIN FETCH laedt das Team gleich mit: vermeidet das N+1-Problem
    @Query("select m from Membership m join fetch m.team t where m.id.userId = :userId order by t.name")
    List<Membership> findAllByUserIdWithTeam(@Param("userId") Long userId);

    @Query("select m from Membership m join fetch m.team where m.id.userId = :userId and m.id.teamId = :teamId")
    Optional<Membership> findByUserIdAndTeamIdWithTeam(@Param("userId") Long userId, @Param("teamId") Long teamId);

    @Query("select m from Membership m join fetch m.user u where m.id.teamId = :teamId order by u.displayName")
    List<Membership> findAllByTeamIdWithUser(@Param("teamId") Long teamId);
}
