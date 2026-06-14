package org.example.sportslivev1.controller;

import org.example.sportslivev1.service.GamesServiceImp;

import org.example.sportslivev1.dto.GameMapper;
import org.example.sportslivev1.dto.GameResponse;
import org.example.sportslivev1.dto.GameDetailResponse;
import org.example.sportslivev1.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/games")
public class GamesController {
    @Autowired
    GamesServiceImp serviceGame;

    @GetMapping
    public ResponseEntity<Page<GameResponse>> getAllGames(@RequestParam(required = false) Games.Status status, @PageableDefault(size = 50, sort = "updatedTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<GameResponse> games = serviceGame.getAllGames(status, pageable).map(GameMapper::toResponse);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDetailResponse> getGameById(@PathVariable Long id) {
        try {
            Games game = serviceGame.getGameById(id);
            return ResponseEntity.status(HttpStatus.OK).body(GameMapper.toDetailResponse(game));
        } 
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game ID not found.");
        }
    }
    
    
}
