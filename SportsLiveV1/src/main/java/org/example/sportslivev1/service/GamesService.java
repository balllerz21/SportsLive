package org.example.sportslivev1.service;

import java.util.List;
import org.example.sportslivev1.entity.Games;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GamesService {
    Games getGameById(Long id);
    Games getGamesbyGameId(String game_id);
    Page<Games> getAllGames(Games.Status stat, Pageable pageable);
    List<Games> getGamesByStatus(Games.Status stat);

}
