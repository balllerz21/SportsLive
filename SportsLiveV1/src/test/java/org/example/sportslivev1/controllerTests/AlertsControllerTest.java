package org.example.sportslivev1.controllerTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.example.sportslivev1.controller.AlertsController;
import org.example.sportslivev1.dto.AlertsRequest;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.GamesServiceImp;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AlertsController.class)
public class AlertsControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AlertsServiceImp alertsService;

        @MockitoBean
        private GamesServiceImp gamesService;

        @Test
        void addAlertTest() throws Exception {
                AlertsRequest req = new AlertsRequest();
                req.setGameId(902L);
                req.setTeamName("Charlotte Hornets");
                req.setAlertType(Alerts.AlertType.SCORE_OVER);
                req.setTargetVal(126);

                Games game = new Games(
                        "401866759",
                        "Phoenix Suns",
                        "Golden State Warriors",
                        111,
                        96,
                        Games.Status.FINAL,
                        Instant.parse("2026-04-18T02:00:00Z")
                );
                game.setId(902L);
                Alerts savedAlert = new Alerts(game, "Charlotte Hornets", Alerts.AlertType.SCORE_OVER, 126);
                savedAlert.setId(1L);

                when(gamesService.getGameById(902L)).thenReturn(game);
                when(alertsService.createAlert(
                        same(game),
                        eq("Charlotte Hornets"),
                        eq(Alerts.AlertType.SCORE_OVER),
                        eq(126)
                )).thenReturn(savedAlert);

                mockMvc.perform(post("/alerts/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isCreated());

                verify(gamesService).getGameById(902L);
                verify(alertsService).createAlert(
                        same(game),
                        eq("Charlotte Hornets"),
                        eq(Alerts.AlertType.SCORE_OVER),
                        eq(126)
                );
        }

        @Test
        void getAlertByIdTest() throws Exception {
                Games game = new Games(
                        "401866759",
                        "Phoenix Suns",
                        "Golden State Warriors",
                        111,
                        96,
                        Games.Status.FINAL,
                        Instant.parse("2026-04-18T02:00:00Z")
                );
                game.setId(204L);

                Alerts alert = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                alert.setId(1L);

                when(alertsService.getAlertById(1L)).thenReturn(alert);

                mockMvc.perform(get("/alerts/1")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$.alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$.targetVal").value(120))
                        .andExpect(jsonPath("$.gameId").value(204))
                        .andExpect(jsonPath("$.actualGameId").value("401866759"))
                        .andExpect(jsonPath("$.homeTeam").value("Phoenix Suns"))
                        .andExpect(jsonPath("$.awayTeam").value("Golden State Warriors"));

                verify(alertsService).getAlertById(1L);
        }

        @Test
        void getAllAlertsTest() throws Exception {
                Games game = new Games(
                        "401866759",
                        "Phoenix Suns",
                        "Golden State Warriors",
                        111,
                        96,
                        Games.Status.FINAL,
                        Instant.parse("2026-04-18T02:00:00Z")
                );
                game.setId(204L);

                Alerts a1 = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                a1.setId(1L);

                Alerts a2 = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_UNDER, 105);
                a2.setId(2L);

                when(alertsService.getAllAlerts(null, null, null, null)).thenReturn(List.of(a1, a2));
                mockMvc.perform(get("/alerts")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(1))
                        .andExpect(jsonPath("$[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$[0].targetVal").value(120))
                        .andExpect(jsonPath("$[1].id").value(2))
                        .andExpect(jsonPath("$[1].teamName").value("Phoenix Suns"))
                        .andExpect(jsonPath("$[1].alertType").value("SCORE_UNDER"))
                        .andExpect(jsonPath("$[1].targetVal").value(105));

                verify(alertsService).getAllAlerts(null, null, null, null);
        }
        @Test
        void getAllAlertsTest2() throws Exception {
        Games game = new Games(
                "401866759",
                "Phoenix Suns",
                "Golden State Warriors",
                111,
                96,
                Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z")
        );
        game.setId(204L);

        Alerts a1 = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
        a1.setId(1L);
        a1.setAlertStatus(Alerts.AlertStatus.TRIGGERED);

        Alerts a2 = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_UNDER, 105);
        a2.setId(2L);

        when(alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, null, null, null)).thenReturn(List.of(a1));
        mockMvc.perform(get("/alerts?status=TRIGGERED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Golden State Warriors"))
                .andExpect(jsonPath("$[0].alertType").value("SCORE_OVER"))
                .andExpect(jsonPath("$[0].targetVal").value(120));

        verify(alertsService).getAllAlerts(Alerts.AlertStatus.TRIGGERED, null, null, null);
        }

        @Test
        void getAllAlertsTest3() throws Exception
        {
                Games game = new Games(
                "401866759",
                "Phoenix Suns",
                "Golden State Warriors",
                111,
                96,
                Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z")
                );
                game.setId(204L);

                Alerts a1 = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                a1.setId(1L);
                a1.setAlertStatus(Alerts.AlertStatus.TRIGGERED);

                when(alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_OVER, null, null)).thenReturn(List.of(a1));
                mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(1))
                        .andExpect(jsonPath("$[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$[0].targetVal").value(120));

                verify(alertsService).getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_OVER, null, null);
        }
        @Test
        void getAllAlertsTest4() throws Exception
        {
                Games game = new Games(
                "401866759",
                "Phoenix Suns",
                "Golden State Warriors",
                111,
                96,
                Games.Status.FINAL,
                Instant.parse("2026-04-18T02:00:00Z")
                );
                game.setId(204L);

                Alerts a1 = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                a1.setId(1L);
                a1.setAlertStatus(Alerts.AlertStatus.TRIGGERED);

                when(alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_OVER, "Golden State Warriors", null)).thenReturn(List.of(a1));
                mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER&teamName=Golden State Warriors")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(1))
                        .andExpect(jsonPath("$[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$[0].targetVal").value(120));

                verify(alertsService).getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_OVER, "Golden State Warriors", null);
        }

        // @Test
        // void getAllAlertsTest5() throws Exception {
        //         Games game = new Games("401866759", "Phoenix Suns", "Golden State Warriors", 111, 96, Games.Status.FINAL, Instant.parse("2026-04-18T02:00:00Z"));
        //         game.setId(204L);

        //         Alerts a1 = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
        //         a1.setId(1L);
        //         a1.setAlertStatus(Alerts.AlertStatus.TRIGGERED);

        //         when(alertsService.getAllAlerts(
        //                 eq(Alerts.AlertStatus.TRIGGERED), 
        //                 eq(Alerts.AlertType.SCORE_OVER), 
        //                 eq("Golden State Warriors"), 
        //                 any(Instant.class) // Handles the Instant conversion mismatch
        //         )).thenReturn(List.of(a1));

        //         mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER&teamName=Golden State Warriors&createdAt=2026-04-18T02:00:00Z\"")
        //                 .accept(MediaType.APPLICATION_JSON))
        //                 .andExpect(status().isOk())
        //                 .andExpect(jsonPath("$[0].id").value(1))
        //                 .andExpect(jsonPath("$[0].teamName").value("Golden State Warriors"));

        //         verify(alertsService).getAllAlerts(
        //                 eq(Alerts.AlertStatus.TRIGGERED), 
        //                 eq(Alerts.AlertType.SCORE_OVER), 
        //                 eq("Golden State Warriors"), 
        //                 any(Instant.class)
        //         );
        // }
        @Test
        void deleteAlertTest() throws Exception {
                mockMvc.perform(delete("/alerts/delete")
                        .param("id", "7"))
                        .andExpect(status().isOk());

                verify(alertsService).deleteAlert(7L);
        }
}