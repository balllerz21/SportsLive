package org.example.sportslivev1.controllerTests;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.example.sportslivev1.auth.AuthEntryPointJwt;
import org.example.sportslivev1.auth.AuthTokenFilter;
import org.example.sportslivev1.config.PageableConfig;
import org.example.sportslivev1.controller.GamesController;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.service.GamesServiceImp;
import org.example.sportslivev1.utils.JwtUtilities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(GamesController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PageableConfig.class)
public class GamesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GamesServiceImp gamesService;
    @MockitoBean
    private JwtUtilities jwtUtilities;
    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;
    @MockitoBean
    private AuthTokenFilter authTokenFilter;
    @MockitoBean
    private AuthEntryPointJwt authEntryPointJwt;

    @Test
    public void getAllGamesTest() throws Exception
    {
        Long id = 1L;
        Long id2 = 2L;
        Games g1 = new Games("401866757", "Philadelphia 76ers", "Orlando Magic", 109, 97, Games.Status.FINAL, Instant.parse("2026-04-18T02:00:00Z"));
        g1.setId(id);
        Games g2 = new Games("401869188", "Denver Nuggets", "Minnesota Timberwolves", 116, 105, Games.Status.FINAL, Instant.parse("2026-04-18T02:00:00Z"));
        g2.setId(id2);
        when(gamesService.getAllGames(
                org.mockito.ArgumentMatchers.isNull(),
                argThat(pageable -> hasPage(pageable, 0, 50, "updatedTime", "DESC"))))
                .thenReturn(new PageImpl<>(List.of(g1, g2)));
        // for testing purposes
        Instant test = Instant.parse("2026-04-18T02:00:00Z");
        mockMvc.perform(get("/games")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].actualGameId").value("401866757"))
                .andExpect(jsonPath("$.content[0].homeTeam").value("Philadelphia 76ers"))
                .andExpect(jsonPath("$.content[0].homeScore").value(109))
                .andExpect(jsonPath("$.content[0].awayTeam").value("Orlando Magic"))
                .andExpect(jsonPath("$.content[0].awayScore").value(97))
                .andExpect(jsonPath("$.content[0].status").value("FINAL"))
                .andExpect(jsonPath("$.content[0].schedTime").value(test.toString()))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].actualGameId").value("401869188"))
                .andExpect(jsonPath("$.content[1].homeTeam").value("Denver Nuggets"))
                .andExpect(jsonPath("$.content[1].homeScore").value(116))
                .andExpect(jsonPath("$.content[1].awayTeam").value("Minnesota Timberwolves"))
                .andExpect(jsonPath("$.content[1].awayScore").value(105))
                .andExpect(jsonPath("$.content[1].status").value("FINAL"))
                .andExpect(jsonPath("$.content[1].schedTime").value(test.toString()))
                .andExpect(jsonPath("$.page.totalElements").value(2));
        verify(gamesService).getAllGames(
                org.mockito.ArgumentMatchers.isNull(),
                argThat(pageable -> hasPage(pageable, 0, 50, "updatedTime", "DESC")));

    }

    @Test
    public void getAllGamesBoundsLimitTest() throws Exception
    {
        when(gamesService.getAllGames(
                org.mockito.ArgumentMatchers.eq(Games.Status.LIVE),
                argThat(pageable -> hasPage(pageable, 0, 100, "updatedTime", "DESC"))))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/games")
                .param("status", "LIVE")
                .param("size", "500")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(gamesService).getAllGames(
                org.mockito.ArgumentMatchers.eq(Games.Status.LIVE),
                argThat(pageable -> hasPage(pageable, 0, 100, "updatedTime", "DESC")));
    }

    @Test
    public void getAllGamesUsesRequestedPageTest() throws Exception
    {
        when(gamesService.getAllGames(
                org.mockito.ArgumentMatchers.isNull(),
                argThat(pageable -> hasPage(pageable, 3, 25, "schedTime", "ASC"))))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/games")
                .param("size", "25")
                .param("page", "3")
                .param("sort", "schedTime,asc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(gamesService).getAllGames(
                org.mockito.ArgumentMatchers.isNull(),
                argThat(pageable -> hasPage(pageable, 3, 25, "schedTime", "ASC")));
    }

    private static boolean hasPage(
            Pageable pageable,
            int page,
            int size,
            String sortProperty,
            String direction) {
        return pageable != null
                && pageable.getPageNumber() == page
                && pageable.getPageSize() == size
                && pageable.getSort().getOrderFor(sortProperty) != null
                && pageable.getSort().getOrderFor(sortProperty).getDirection().name().equals(direction);
    }
    @Test
    public void getGamesByIdTest() throws Exception
    {
        Long id = 1L;
        Games g1 = new Games("401866757", "Philadelphia 76ers", "Orlando Magic", 109, 97, Games.Status.FINAL, Instant.parse("2026-04-18T02:00:00Z"));
        g1.setId(id);
        Alerts a1 = new Alerts(g1, "Philadelphia 76ers", Alerts.AlertType.SCORE_OVER, 100);
        a1.setId(902L);
        g1.setAlerts(List.of(a1));
        when(gamesService.getGameById(id)).thenReturn(g1);
        mockMvc.perform(get("/games/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.actualGameId").value("401866757"))
                .andExpect(jsonPath("$.homeTeam").value("Philadelphia 76ers"))
                .andExpect(jsonPath("$.homeScore").value(109))
                .andExpect(jsonPath("$.awayTeam").value("Orlando Magic"))
                .andExpect(jsonPath("$.awayScore").value(97))
                .andExpect(jsonPath("$.status").value("FINAL"))
                .andExpect(jsonPath("$.schedTime").value("2026-04-18T02:00:00Z"))
                .andExpect(jsonPath("$.alerts[0].id").value(902L))
                .andExpect(jsonPath("$.alerts[0].teamName").value("Philadelphia 76ers"))
                .andExpect(jsonPath("$.alerts[0].alertType").value("SCORE_OVER"))
                .andExpect(jsonPath("$.alerts[0].targetVal").value(100));


        verify(gamesService).getGameById(1L);
    }

}
