package org.example.sportslivev1.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.sportslivev1.client.EspnClientAPI;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.repository.GamesRepo;
import org.springframework.stereotype.Service;

@Service
public class PollingService {

    private final EspnClientAPI espnApiClient;
    private final GamesRepo gamesRepo;
    private final GamesServiceImp gamesService;

    public PollingService(EspnClientAPI espnApiClient, GamesRepo gamesRepo, GamesServiceImp gamesService) {
        this.espnApiClient = espnApiClient;
        this.gamesService = gamesService;
        this.gamesRepo = gamesRepo;
    }

    private List<Games> extractAndSetGame(JsonNode reader) {
        List<Games> games = new ArrayList<>();
        JsonNode events = reader.get("events");

        for (JsonNode ev : events) {
            int homeScore = 0;
            int awayScore = 0;
            String home = null;
            String away = null;
            Games.Status status;

            String gameId = ev.get("id").asString();
            String start = ev.get("date").asString();
            Instant date = OffsetDateTime.parse(start).toInstant();

            JsonNode competition = ev.get("competitions").get(0);
            JsonNode competitors = competition.get("competitors");

            for (JsonNode d : competitors) {
                String homeAway = d.get("homeAway").asString();
                int score = d.get("score").asInt();
                JsonNode teamDetails = d.get("team");
                String teamName = teamDetails.get("displayName").asString();

                if ("home".equalsIgnoreCase(homeAway)) {
                    homeScore = score;
                    home = teamName;
                } else if ("away".equalsIgnoreCase(homeAway)) {
                    awayScore = score;
                    away = teamName;
                }
            }

            JsonNode statusDetails = competition.get("status").get("type");
            String statusType = statusDetails.get("state").asString();

            if ("pre".equalsIgnoreCase(statusType)) {
                status = Games.Status.SCHEDULED;
            } else if ("in".equalsIgnoreCase(statusType)) {
                status = Games.Status.LIVE;
            } else if ("post".equalsIgnoreCase(statusType)) {
                status = Games.Status.FINAL;
            } else {
                continue;
            }

            Games game = gamesRepo.findByActualGameId(gameId);

            if (game == null) {
                game = new Games(gameId, home, away, homeScore, awayScore, status, date);
                games.add(game);
            } else if (game.getHomeScore() != homeScore
                    || game.getAwayScore() != awayScore
                    || game.getStatus() != status) {

                game.setHomeScore(homeScore);
                game.setAwayScore(awayScore);
                game.setStatus(status);
                game.setUpdatedTime(Instant.now());
                games.add(game);
            }
            else{
                continue;
            }
        }

        return games;
    }

    public void createOrUpdateGame(String site) {
        List<Games> games = new ArrayList<>();
        String rawJson = espnApiClient.getScoreBoard(site);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode reader = objectMapper.readTree(rawJson);
            games = extractAndSetGame(reader);

            for (Games g : games) {
                gamesService.saveGame(g);
            }

        } catch (Exception e) {
            System.out.println("Failed to parse ESPN response: " + e.getMessage());
        }
    }
}