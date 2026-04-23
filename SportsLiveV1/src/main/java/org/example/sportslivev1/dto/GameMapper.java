package org.example.sportslivev1.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.example.sportslivev1.entity.Games;

public class GameMapper {
    // all games page (dashboard)
    public static GameResponse toResponse(Games game) {
        GameResponse dto = new GameResponse();

        dto.setId(game.getId());
        dto.setActualGameId(game.getActualGameId());
        dto.setHomeTeam(game.getHomeTeam());
        dto.setAwayTeam(game.getAwayTeam());
        dto.setHomeScore(game.getHomeScore());
        dto.setAwayScore(game.getAwayScore());
        dto.setSchedTime(game.getSchedTime());
        dto.setUpdatedTime(game.getUpdatedTime());
        dto.setStatus(game.getStatus().name());

        return dto;
    }
    // game_id page
    public static GameDetailResponse toDetailResponse(Games game) {
        GameDetailResponse dto = new GameDetailResponse();
        dto.setId(game.getId());
        dto.setActualGameId(game.getActualGameId());
        dto.setHomeTeam(game.getHomeTeam());
        dto.setAwayTeam(game.getAwayTeam());
        dto.setHomeScore(game.getHomeScore());
        dto.setAwayScore(game.getAwayScore());
        dto.setSchedTime(game.getSchedTime());
        dto.setUpdatedTime(game.getUpdatedTime());
        dto.setStatus(game.getStatus().name());

        if (game.getAlerts() == null) {
            dto.setAlerts(Collections.emptyList());
        } else {
            // this is how to add alerts to the DTO (immuatble)
            dto.setAlerts(game.getAlerts().stream().map(AlertMapper::toSummaryResponse).collect(Collectors.toList()));
        }

        return dto;
    }
}