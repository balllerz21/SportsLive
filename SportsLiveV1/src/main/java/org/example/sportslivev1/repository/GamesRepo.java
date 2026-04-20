package org.example.sportslivev1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.*;
import java.time.LocalDateTime;

// Adding later: more querying on teams and time.
@Repository
public interface GamesRepo extends JpaRepository<Games, Long> {
    List<Games> findByHomeTeam(String homeTeam);
    List<Games> findByAwayTeam(String awayTeam);
    List<Games> findBySchedTimeOrderBySchedTimeAsc(LocalDateTime schedTime);
    List<Games>findByUpdatedTime(LocalDateTime updatedTime);
    // only ones used in implementation for now
    List<Games>findByStatus(Games.Status status);
    Optional<Games> findById(Long id);
    Games findByActualGameId(String actualGameId);

}
