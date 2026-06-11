package org.example.sportslivev1.serviceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import org.example.sportslivev1.client.EspnClientAPI;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.service.GamesServiceImp;
import org.example.sportslivev1.service.PollingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PollingServiceTest {

    @Mock
    private EspnClientAPI espnApiClient;

    @Mock
    private GamesServiceImp gamesService;

    @Mock
    private GamesRepo gamesRepo;

    @InjectMocks
    private PollingService pollingService;

    @Test
    void createOrUpdateGame_preStatus_becomesScheduled() {
        String rawJson = """
        {
        "events": [
            {
            "id": "401866758",
            "date": "2026-04-17T23:30Z",
            "competitions": [
                {
                "competitors": [
                    {
                    "homeAway": "home",
                    "score": "0",
                    "team": { "displayName": "Orlando Magic" }
                    },
                    {
                    "homeAway": "away",
                    "score": "0",
                    "team": { "displayName": "Charlotte Hornets" }
                    }
                ],
                "status": {
                    "type": {
                    "state": "pre"
                    }
                }
                }
            ]
            }
        ]
        }
        """;

        when(espnApiClient.getScoreBoard("nba")).thenReturn(rawJson);
        ArgumentCaptor<Games> gameCaptor = ArgumentCaptor.forClass(Games.class);

        pollingService.createOrUpdateGame("nba");

        verify(gamesService).saveGame(gameCaptor.capture());

        Games savedGame = gameCaptor.getValue();
        assertEquals("401866758", savedGame.getActualGameId());
        assertEquals(Games.Status.SCHEDULED, savedGame.getStatus());
        assertEquals("Orlando Magic", savedGame.getHomeTeam());
        assertEquals("Charlotte Hornets", savedGame.getAwayTeam());
        assertEquals(0, savedGame.getHomeScore());
        assertEquals(0, savedGame.getAwayScore());
    }
}