package org.example.sportslivev1.serviceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.service.GamesServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GamesServiceTest {
    @Mock
    private GamesRepo gamesRepo;

    @InjectMocks
    private GamesServiceImp gamesService;

    private Games buildGame() {
        return new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,
            110,
            Games.Status.FINAL,
            Instant.parse("2026-04-18T02:00:00Z")
        );
    }

    @Test
    public void createGameTest() {
        Instant scheduled = Instant.parse("2026-04-18T02:00:00Z");

        gamesService.createGame(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,
            110,
            Games.Status.FINAL,
            scheduled
        );

        ArgumentCaptor<Games> captor = ArgumentCaptor.forClass(Games.class);
        verify(gamesRepo).save(captor.capture());

        Games savedGame = captor.getValue();
        assertEquals("401866759", savedGame.getActualGameId());
        assertEquals("Phoenix Suns", savedGame.getHomeTeam());
        assertEquals("Golden State Warriors", savedGame.getAwayTeam());
        assertEquals(121, savedGame.getHomeScore());
        assertEquals(110, savedGame.getAwayScore());
        assertEquals(Games.Status.FINAL, savedGame.getStatus());
        assertEquals(scheduled, savedGame.getSchedTime());
    }

    @Test
    public void getGameByIdTest1() {
        Long id = 1L;
        Games game = buildGame();

        when(gamesRepo.findById(id)).thenReturn(Optional.of(game));

        Games result = gamesService.getGameById(id);

        assertNotNull(result);
        assertEquals(game, result);
    }

    @Test
    public void getGameByIdTest2() {
        Long id = 99L;

        when(gamesRepo.findById(id)).thenReturn(Optional.empty());

        Games result = gamesService.getGameById(id);

        assertNull(result);
    }

    @Test
    public void getGamesbyGameIdTest() {
        String actualGameId = "401866759";
        Games game = buildGame();

        when(gamesRepo.findByActualGameId(actualGameId)).thenReturn(game);

        Games result = gamesService.getGamesbyGameId(actualGameId);

        assertNotNull(result);
        assertEquals(game, result);
    }

    @Test
    public void getAllGamesTest() {
        Games game1 = buildGame();
        Games game2 = new Games(
            "401866758",
            "Orlando Magic",
            "Charlotte Hornets",
            0,
            0,
            Games.Status.SCHEDULED,
            Instant.parse("2026-04-17T23:30:00Z")
        );

        when(gamesRepo.findAll()).thenReturn(List.of(game1, game2));

        List<Games> result = gamesService.getAllGames();

        assertEquals(2, result.size());
        assertEquals(game1, result.get(0));
        assertEquals(game2, result.get(1));
    }

    @Test
    public void getGamesByStatusTest() {
        Games liveGame = new Games(
            "401866760",
            "Lakers",
            "Celtics",
            88,
            85,
            Games.Status.LIVE,
            Instant.parse("2026-04-18T01:00:00Z")
        );

        when(gamesRepo.findByStatus(Games.Status.LIVE)).thenReturn(List.of(liveGame));

        List<Games> result = gamesService.getGamesByStatus(Games.Status.LIVE);

        assertEquals(1, result.size());
        assertEquals(Games.Status.LIVE, result.get(0).getStatus());
        assertEquals(liveGame, result.get(0));
    }

    @Test
    public void saveGameTest() {
        Games game = buildGame();

        when(gamesRepo.save(game)).thenReturn(game);

        gamesService.saveGame(game);
        ArgumentCaptor<Games> captor = ArgumentCaptor.forClass(Games.class);
        verify(gamesRepo).save(captor.capture());
        Games result = captor.getValue();
        assertNotNull(result);
        assertEquals(game, result);
        verify(gamesRepo).save(game);
    }
}
