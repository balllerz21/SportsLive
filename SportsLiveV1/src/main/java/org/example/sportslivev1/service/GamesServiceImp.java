package org.example.sportslivev1.service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.*;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.specifications.GamesSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
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
    public Page<Games> getAllGames(Games.Status stat, Pageable pageable)
    {
        Specification<Games> spec = Specification.unrestricted();
        if (stat != null)
        {
            spec = spec.and(GamesSpecifications.hasStatus(stat));
        }
        return gamesRepo.findAll(spec, pageable);
    }
    @Override
    public List<Games> getGamesByStatus(Games.Status stat)
    {
        return gamesRepo.findByStatus(stat);
    }

    @Transactional
    public void saveGame(Games game) {
        gamesRepo.save(game);
    }
    @Async
    public void createOrUpdateGames(Instant date)
    {
        //TODO: Make logic here. U will need a table that will have access to this for every user call it sessions table or somethimg.
        // - OneToMany relationship
        // - There store log in timestamp
        // - compare it and then add missed games to the app. 
        // - make sure no duplicates by checking the game_id exists
        // - if it exists then just update it
    }
}
