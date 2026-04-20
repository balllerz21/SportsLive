package org.example.sportslivev1.dto;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;

public class AlertMapper {
    public static AlertResponse toResponse(Alerts alert) {
        AlertResponse dto = new AlertResponse();

        dto.setId(alert.getId());
        dto.setTeamName(alert.getTeamName());
        dto.setAlertType(alert.getAlertType());
        dto.setTargetVal(alert.getTargetVal());
        dto.setStatus(alert.getAlertStatus());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setTriggeredAt(alert.getTriggeredAt());

        Games game = alert.getGame();
        if (game != null) {
            dto.setGameId(game.getId());
            dto.setActualGameId(game.getActualGameId());
            dto.setHomeTeam(game.getHomeTeam());
            dto.setAwayTeam(game.getAwayTeam());
            dto.setHomeScore(game.getHomeScore());
            dto.setAwayScore(game.getAwayScore());
            dto.setGameStatus(game.getStatus().name());
        }

        return dto;
    }
    public static AlertListResponse toSummaryResponse(Alerts alert) {
        AlertListResponse dto = new AlertListResponse();
        dto.setId(alert.getId());
        dto.setTeamName(alert.getTeamName());
        dto.setAlertType(alert.getAlertType());
        dto.setTargetVal(alert.getTargetVal());
        dto.setStatus(alert.getAlertStatus());
        return dto;
    }
}