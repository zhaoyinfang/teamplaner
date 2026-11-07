package dev.teamplaner;

import dev.teamplaner.team.Membership;
import dev.teamplaner.team.MembershipRepository;
import dev.teamplaner.team.Team;
import dev.teamplaner.team.TeamRepository;
import dev.teamplaner.team.TeamRole;
import dev.teamplaner.user.User;
import dev.teamplaner.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Startet eine echte PostgreSQL im Container, laesst Flyway das Schema anlegen und prueft,
 * dass die Entities zum Schema passen (ddl-auto: validate).
 */
@SpringBootTest
@Testcontainers
class SchemaIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired UserRepository users;
    @Autowired TeamRepository teams;
    @Autowired MembershipRepository memberships;

    @Test
    void userCanBeOwnerOfTeam_andIsFoundViaMembership() {
        User u = new User();
        u.setEmail("schema-test@example.com");
        u.setPasswordHash("x");
        u.setDisplayName("Trainer");
        u = users.save(u);

        Team t = new Team();
        t.setName("SG Beispiel");
        t.setInviteCode("TESTCODE");
        t = teams.save(t);

        memberships.save(new Membership(u, t, TeamRole.OWNER));

        assertThat(teams.findAllByMemberUserId(u.getId())).hasSize(1);
        assertThat(memberships.existsByIdUserIdAndIdTeamIdAndRole(u.getId(), t.getId(), TeamRole.OWNER)).isTrue();
    }
}
