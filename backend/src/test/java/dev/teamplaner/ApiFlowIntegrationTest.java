package dev.teamplaner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Durchlaeuft den kompletten Ablauf der App ueber die REST-API gegen eine echte PostgreSQL. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void fullFlow() throws Exception {
        String owner = register("owner@example.com");
        String member = register("member@example.com");
        String outsider = register("outsider@example.com");

        // ohne Token: 401
        mvc.perform(get("/api/events")).andExpect(status().isUnauthorized());

        // doppelte Registrierung: 409, falsches Passwort: 401
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "owner@example.com", "password", "password123", "displayName", "x"))))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "owner@example.com", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());

        // Owner legt Team an und sieht den Einladungscode
        MvcResult created = mvc.perform(post("/api/teams").header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "SG Beispiel"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andReturn();
        JsonNode team = om.readTree(created.getResponse().getContentAsString());
        long teamId = team.get("id").asLong();
        String code = team.get("inviteCode").asText();

        // Mitglied tritt per Code bei und sieht den Code nicht
        mvc.perform(post("/api/teams/join").header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("inviteCode", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.inviteCode").doesNotExist());

        // Aussenstehende sehen weder Team noch Termine
        mvc.perform(get("/api/teams/" + teamId + "/events").header("Authorization", bearer(outsider)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/teams").header("Authorization", bearer(outsider)))
                .andExpect(jsonPath("$", hasSize(0)));

        // Termin: nur Owner darf anlegen
        Map<String, Object> eventBody = Map.of(
                "title", "Training",
                "type", "TRAINING",
                "location", "Sportplatz",
                "startsAt", Instant.now().plus(Duration.ofDays(2)).toString());
        mvc.perform(post("/api/teams/" + teamId + "/events").header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content(json(eventBody)))
                .andExpect(status().isForbidden());
        MvcResult ev = mvc.perform(post("/api/teams/" + teamId + "/events").header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON).content(json(eventBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseOpen").value(true))
                .andReturn();
        long eventId = om.readTree(ev.getResponse().getContentAsString()).get("id").asLong();

        // Mitglied sagt zu
        mvc.perform(put("/api/events/" + eventId + "/attendance").header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.counts.accepted").value(1));

        // ... und ueberlegt es sich anders
        mvc.perform(put("/api/events/" + eventId + "/attendance").header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "DECLINED"))))
                .andExpect(jsonPath("$.counts.accepted").value(0))
                .andExpect(jsonPath("$.counts.declined").value(1));

        // Aussenstehende duerfen nicht antworten
        mvc.perform(put("/api/events/" + eventId + "/attendance").header("Authorization", bearer(outsider))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "ACCEPTED"))))
                .andExpect(status().isForbidden());

        // Dashboard des Mitglieds zeigt genau diesen Termin
        mvc.perform(get("/api/events").header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].teamName").value("SG Beispiel"));
    }

    private String register(String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "password123", "displayName", email))))
                .andExpect(status().isCreated())
                .andReturn();
        return om.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String json(Object o) throws Exception {
        return om.writeValueAsString(o);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
