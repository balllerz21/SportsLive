package org.example.sportslivev1.service;

import java.util.List;
import org.example.sportslivev1.entity.Games;

public interface GamesService {
    Games getGameById(Long id);
    Games getGamesbyGameId(String game_id);
    List<Games> getAllGames(Games.Status stat);
    List<Games> getGamesByStatus(Games.Status stat);

}
