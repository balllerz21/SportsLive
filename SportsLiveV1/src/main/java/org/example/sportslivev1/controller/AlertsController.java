package org.example.sportslivev1.controller;

import org.example.sportslivev1.service.AlertsServiceImp;
import org.example.sportslivev1.service.GamesServiceImp;
import org.example.sportslivev1.dto.AlertMapper;
import org.example.sportslivev1.dto.AlertResponse;
import org.example.sportslivev1.dto.AlertsRequest;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertType;
import org.example.sportslivev1.entity.Games;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

// add error handiling w exceptions
@RestController
@RequestMapping("/alerts")
public class AlertsController {
    @Autowired
    AlertsServiceImp service;
    @Autowired
    GamesServiceImp service2;
    
    @PostMapping("/add")
    public void add(@RequestBody AlertsRequest alert) {
        service.createAlert(
            service2.getGameById(alert.getGameId()),
            alert.getTeamName(),
            alert.getAlertType(),
            alert.getTargetVal()
        );
    }
    
    @GetMapping
    public List<AlertResponse> getAllAlerts(@RequestParam(required = false) String team, @RequestParam(required = false) AlertType type) {
        List<Alerts> alerts;
        if (team != null && type != null) {
            alerts = service.getAlertsByTypeAndTeam(team, type);
        } else if (team != null) {
            alerts = service.getAlertsByTeamName(team);
        } else if (type != null) {
            alerts = service.getAlertsByAlertType(type);
        } else {
            alerts = service.getAllAlerts();
        }
        return alerts.stream().map(AlertMapper::toResponse).toList();
    }
    @GetMapping("/{id}")
    public AlertResponse alertById(@PathVariable Long id) {
        Alerts alert = service.getAlertById(id);
        return AlertMapper.toResponse(alert);
    }
    // @GetMapping("/team")
    // public List<Alerts> alertsByTeam(@RequestParam String team) {
    //     return service.getAlertsByTeamName(team);
    // }
    // @GetMapping("/status")
    // public List<Alerts> alertsByStatus(@RequestParam boolean status) {
    //     return service.getAlertsByisActive(status);
    // }
    // @GetMapping("/time")
    // public List<Alerts> alertsByDate(@RequestParam LocalDateTime time) {
    //     return service.getAlertsByTime(time);
    // }
    // @GetMapping("/type")
    // public List<Alerts> alertsByType(@RequestParam AlertType type) {
    //     return service.getAlertsByAlertType(type);
    // }
    @DeleteMapping("/delete")
    public void delete(@RequestParam Long id){
        service.deleteAlert(id);
    }
    

}
