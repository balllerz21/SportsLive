package org.example.sportslivev1.service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.*;
import org.example.sportslivev1.repository.GamesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class GamesServiceImp implements GamesService {
    @Autowired
    GamesRepo gamesRepo;

	public void createGame(String game_id, String home, String away, int home_score, int away_score, Games.Status status, Instant scheduled)
    {
        Games game = new Games(game_id, home, away, home_score, away_score, status, scheduled);
        gamesRepo.save(game);
    }
    @Override
    public Games getGameById(Long id)
    {   
        Optional<Games> game = gamesRepo.findById(id);
        if (game.isPresent())
        {
            return game.get();
        }
        else
        {
            throw new EntityNotFoundException("Game ID not found");
        }
    }
    @Override
    public Games getGamesbyGameId(String gane_id)
    {
        return gamesRepo.findByActualGameId(gane_id);
    }
     @Override
    public List<Games> getAllGames()
    {
        return gamesRepo.findAll();
    }
    @Override
    public List<Games> getGamesByStatus(Games.Status stat)
    {
        return gamesRepo.findByStatus(stat);
    }

    // Why do we need this? If we have createGame which does the same thing minus the Game thing on top
    @Transactional
    public void saveGame(Games game) {
        gamesRepo.save(game);
    }
}
