package org.example.sportslivev1.controllerTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

import org.example.sportslivev1.auth.AuthEntryPointJwt;
import org.example.sportslivev1.auth.AuthTokenFilter;
import org.example.sportslivev1.config.PageableConfig;
import org.example.sportslivev1.controller.AlertsController;
import org.example.sportslivev1.dto.AlertsRequest;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.entity.Users.UserRole;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.GamesServiceImp;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.example.sportslivev1.utils.JwtUtilities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AlertsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PageableConfig.class)
public class AlertsControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AlertsServiceImp alertsService;

        @MockitoBean
        private GamesServiceImp gamesService;
        @MockitoBean
        private UsersServiceImpl usersService;

        @MockitoBean
        private JwtUtilities jwtUtilities;
        @MockitoBean
        private BCryptPasswordEncoder passwordEncoder;
        @MockitoBean
        private AuthTokenFilter authTokenFilter;
        @MockitoBean
        private AuthEntryPointJwt authEntryPointJwt;

        private Users user(String username, Long id) {
                Users user = new Users(username, "password", UserRole.USER);
                user.setId(id);
                return user;
        }

        private UsernamePasswordAuthenticationToken principal(String username) {
                return new UsernamePasswordAuthenticationToken(username, null);
        }

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
                Users user = user("testuser", 1L);

                game.setId(902L);
                Alerts savedAlert = new Alerts(game, "Charlotte Hornets", Alerts.AlertType.SCORE_OVER, 126);
                savedAlert.setUser(user);

                when(gamesService.getGameById(902L)).thenReturn(game);
                when(usersService.getUserByUserName("testuser")).thenReturn(user);
                when(alertsService.createAlert(
                        same(game),
                        same(user),
                        eq("Charlotte Hornets"),
                        eq(Alerts.AlertType.SCORE_OVER),
                        eq(126)
                )).thenReturn(savedAlert);

                mockMvc.perform(post("/alerts/add")
                        .principal(principal("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isCreated());

                verify(gamesService).getGameById(902L);
                verify(usersService).getUserByUserName("testuser");
                verify(alertsService).createAlert(
                        same(game),
                        same(user),
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
                alert.setUser(user("testuser", 1L));

                when(alertsService.getAlertById(1L)).thenReturn(alert);

                mockMvc.perform(get("/alerts/1")
                        .principal(principal("testuser"))
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
                a1.setUser(user("testuser", 1L));

                Alerts a2 = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_UNDER, 105);
                a2.setId(2L);
                a2.setUser(user("testuser", 1L));

                when(alertsService.getAllAlerts(isNull(), isNull(), isNull(), isNull(), eq("testuser"), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(a1, a2)));
                mockMvc.perform(get("/alerts")
                        .principal(principal("testuser"))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].id").value(1))
                        .andExpect(jsonPath("$.content[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$.content[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$.content[0].targetVal").value(120))
                        .andExpect(jsonPath("$.content[1].id").value(2))
                        .andExpect(jsonPath("$.content[1].teamName").value("Phoenix Suns"))
                        .andExpect(jsonPath("$.content[1].alertType").value("SCORE_UNDER"))
                        .andExpect(jsonPath("$.content[1].targetVal").value(105))
                        .andExpect(jsonPath("$.page.totalElements").value(2));

                verify(alertsService).getAllAlerts(isNull(), isNull(), isNull(), isNull(), eq("testuser"), any(Pageable.class));
        }

        @Test
        void getAllAlertsFiltersByAuthenticatedUserTest() throws Exception {
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

                Users user = user("testuser", 1L);
                Users otherUser = user("otheruser", 2L);

                Alerts userAlert = new Alerts(game, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                userAlert.setId(1L);
                userAlert.setUser(user);

                Alerts otherAlert = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_UNDER, 105);
                otherAlert.setId(2L);
                otherAlert.setUser(otherUser);

                when(alertsService.getAllAlerts(eq(Alerts.AlertStatus.CREATED), isNull(), isNull(), isNull(), eq("testuser"), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(userAlert)));

                mockMvc.perform(get("/alerts?status=CREATED")
                        .principal(new UsernamePasswordAuthenticationToken("testuser", null))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(1))
                        .andExpect(jsonPath("$.content[0].id").value(1))
                        .andExpect(jsonPath("$.content[0].teamName").value("Golden State Warriors"));

                verify(alertsService).getAllAlerts(eq(Alerts.AlertStatus.CREATED), isNull(), isNull(), isNull(), eq("testuser"), any(Pageable.class));
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
        a1.setUser(user("testuser", 1L));

        Alerts a2 = new Alerts(game, "Phoenix Suns", Alerts.AlertType.SCORE_UNDER, 105);
        a2.setId(2L);

        when(alertsService.getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), isNull(), isNull(), isNull(), eq("testuser"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a1)));
        mockMvc.perform(get("/alerts?status=TRIGGERED")
                .principal(principal("testuser"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].teamName").value("Golden State Warriors"))
                .andExpect(jsonPath("$.content[0].alertType").value("SCORE_OVER"))
                .andExpect(jsonPath("$.content[0].targetVal").value(120));

        verify(alertsService).getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), isNull(), isNull(), isNull(), eq("testuser"), any(Pageable.class));
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
                a1.setUser(user("testuser", 1L));

                when(alertsService.getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), isNull(), isNull(), eq("testuser"), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(a1)));
                mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER")
                        .principal(principal("testuser"))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].id").value(1))
                        .andExpect(jsonPath("$.content[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$.content[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$.content[0].targetVal").value(120));

                verify(alertsService).getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), isNull(), isNull(), eq("testuser"), any(Pageable.class));
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
                a1.setUser(user("testuser", 1L));

                when(alertsService.getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), eq("Golden State Warriors"), isNull(), eq("testuser"), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(a1)));
                mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER&teamName=Golden State Warriors")
                        .principal(principal("testuser"))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].id").value(1))
                        .andExpect(jsonPath("$.content[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$.content[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$.content[0].targetVal").value(120));

                verify(alertsService).getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), eq("Golden State Warriors"), isNull(), eq("testuser"), any(Pageable.class));
        }
        @Test
        void getAllAlertsTest5() throws Exception
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
                a1.setCreatedAt(Instant.parse("2026-04-18T02:00:00Z"));
                a1.setUser(user("testuser", 1L));

                when(alertsService.getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), eq("Golden State Warriors"), eq("weekly"), eq("testuser"), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(a1)));
                mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER&teamName=Golden State Warriors&period=weekly")
                        .principal(principal("testuser"))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].id").value(1))
                        .andExpect(jsonPath("$.content[0].teamName").value("Golden State Warriors"))
                        .andExpect(jsonPath("$.content[0].alertType").value("SCORE_OVER"))
                        .andExpect(jsonPath("$.content[0].targetVal").value(120));

                verify(alertsService).getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), eq("Golden State Warriors"), eq("weekly"), eq("testuser"), any(Pageable.class));
        }
        @Test
        void getAllAlertsTestErrorInvalidTimeframeParam() throws Exception
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
                a1.setCreatedAt(Instant.parse("2026-04-18T02:00:00Z"));

                when(alertsService.getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), eq("Golden State Warriors"), eq("hello"), eq("testuser"), any(Pageable.class)))
                        .thenThrow(new IllegalArgumentException("Invalid timeframe format. Use formats like 'daily', 'weekly', 'monthly', or 'yearly'."));
                mockMvc.perform(get("/alerts?status=TRIGGERED&alertType=SCORE_OVER&teamName=Golden State Warriors&period=hello")
                        .principal(principal("testuser"))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
                verify(alertsService).getAllAlerts(eq(Alerts.AlertStatus.TRIGGERED), eq(Alerts.AlertType.SCORE_OVER), eq("Golden State Warriors"), eq("hello"), eq("testuser"), any(Pageable.class));
        }
        @Test
        void deleteAlertTest() throws Exception {
                Alerts alert = new Alerts(null, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                alert.setId(7L);
                alert.setUser(user("testuser", 1L));

                when(alertsService.getAlertById(7L)).thenReturn(alert);

                mockMvc.perform(delete("/alerts/delete")
                        .principal(principal("testuser"))
                        .param("id", "7"))
                        .andExpect(status().isOk());

                verify(alertsService).getAlertById(7L);
                verify(alertsService).deleteAlert(7L);
        }

        @Test
        void acknowledgeAlertSetsNotifiedAtTest() throws Exception {
                Alerts alert = new Alerts(null, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                alert.setId(7L);
                alert.setUser(user("testuser", 1L));

                when(alertsService.getAlertById(7L)).thenReturn(alert);
                when(alertsService.saveAlert(any(Alerts.class))).thenAnswer(invocation -> invocation.getArgument(0));

                mockMvc.perform(post("/alerts/7/ack")
                        .principal(principal("testuser")))
                        .andExpect(status().isNoContent());

                verify(alertsService).getAlertById(7L);
                verify(alertsService).saveAlert(alert);
        }

        @Test
        void acknowledgeAlertRejectsWrongUserTest() throws Exception {
                Alerts alert = new Alerts(null, "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 120);
                alert.setId(7L);
                alert.setUser(user("otheruser", 2L));

                when(alertsService.getAlertById(7L)).thenReturn(alert);

                mockMvc.perform(post("/alerts/7/ack")
                        .principal(principal("testuser")))
                        .andExpect(status().isForbidden());

                verify(alertsService).getAlertById(7L);
        }
}
