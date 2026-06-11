package org.example.sportslivev1.controller;

import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.GamesServiceImp;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.example.sportslivev1.dto.AlertMapper;
import org.example.sportslivev1.dto.AlertResponse;
import org.example.sportslivev1.dto.AlertsRequest;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Alerts.AlertType;
import org.example.sportslivev1.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/alerts")
public class AlertsController {

    @Autowired
    AlertsServiceImp service;
    @Autowired
    GamesServiceImp service2;
    @Autowired
    UsersServiceImpl service3;
    
    @PostMapping("/add")
    public ResponseEntity<AlertResponse> add(@RequestBody AlertsRequest alert, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        try{
            Users user = service3.getUserByUserName(principal.getName());
            Alerts a = service.createAlert(
                service2.getGameById(alert.getGameId()),
                user,
                alert.getTeamName(),
                alert.getAlertType(),
                alert.getTargetVal()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(AlertMapper.toResponse(a));
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts(@RequestParam(required = false) String teamName, @RequestParam(required = false) AlertType alertType, @RequestParam(required = false) AlertStatus status, @RequestParam(required = false) String period, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        try {
            List<Alerts> alerts;
            alerts = service.getAllAlerts(status, alertType, teamName, period);
            alerts = alerts.stream()
                .filter(alert -> alert.getUser() != null)
                .filter(alert -> principal.getName().equals(alert.getUser().getUserName()))
                .toList();
            return ResponseEntity.ok(alerts.stream().map(AlertMapper::toResponse).toList());
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid query parameters");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> alertById(@PathVariable Long id, Principal principal) {
        try {
            Alerts alert = service.getAlertById(id);
            if (principal == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
            }
            if (alert.getUser() == null || !principal.getName().equals(alert.getUser().getUserName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this alert");
            }
            return ResponseEntity.ok(AlertMapper.toResponse(alert));
        }
        catch (EntityNotFoundException e)
        {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "entity not found"
            );
        }

    }
    @DeleteMapping("/delete")
    public void delete(@RequestParam Long id, Principal principal){
        Alerts alert = service.getAlertById(id);
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        if (alert.getUser() == null || !principal.getName().equals(alert.getUser().getUserName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this alert");
        }
        service.deleteAlert(id);
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        try {
            Alerts alert = service.getAlertById(id);
            if (alert.getUser() == null || !principal.getName().equals(alert.getUser().getUserName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this alert");
            }

            alert.setNotifiedAt(Instant.now());
            service.saveAlert(alert);
            return ResponseEntity.noContent().build();
        }
        catch (EntityNotFoundException e)
        {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }
    

}
