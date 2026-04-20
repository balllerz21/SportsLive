package org.example.sportslivev1.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.example.sportslivev1.entity.Games;

public interface GamesService {
    Games getGameById(Long id);
    Games getGamesbyGameId(String game_id);
    List<Games> getAllGames();
    List<Games> getGamesByStatus(Games.Status stat);

}
