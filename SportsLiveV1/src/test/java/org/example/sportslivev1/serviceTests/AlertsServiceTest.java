package org.example.sportslivev1.serviceTests;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Alerts.AlertType;
import org.example.sportslivev1.repository.AlertsRepo;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.service.AlertsService;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityNotFoundException;

// DONE: Add Alerts test for edited getAllAlerts()

@ExtendWith(MockitoExtension.class)
public class AlertsServiceTest {
    @Mock
    private AlertsRepo alertsRepo;

    @Mock
    private AlertsService service;

    @Mock
    private GamesRepo gamesRepo;

    @InjectMocks
    private AlertsServiceImp alertsService;

    private Alerts buildAlerts()
    {
        Games test = new Games("401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,
            110,
            Games.Status.FINAL,
            Instant.parse("2026-04-18T02:00:00Z"));
        Alerts alert = new Alerts(test, "Phoenix Suns", Alerts.AlertType.SCORE_OVER, 120);
        return alert;
    }

    @Test
    public void createAlertTest() {
        Long id = 1L;
        Games test = new Games("401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,
            110,
            Games.Status.FINAL,
            Instant.parse("2026-04-18T02:00:00Z"));
        test.setId(id);
        when(gamesRepo.findById(id)).thenReturn(Optional.of(test));

        alertsService.createAlert(test, "Phoenix Suns", Alerts.AlertType.SCORE_OVER, 120);

        ArgumentCaptor<Alerts> alerts_dets = ArgumentCaptor.forClass(Alerts.class);
        verify(alertsRepo).save(alerts_dets.capture());

        Alerts testAlert = alerts_dets.getValue();
        assertSame(test, testAlert.getGame());
        assertEquals("Phoenix Suns", testAlert.getTeamName());
        assertEquals(Alerts.AlertType.SCORE_OVER, testAlert.getAlertType());
        assertEquals(120, testAlert.getTargetVal());
        assertEquals(Alerts.AlertStatus.CREATED, testAlert.getAlertStatus());

        verify(gamesRepo).findById(id);
    }
    @Test
    public void createAlertTest2() {
        Long id = 1L;
        Games test = new Games("401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,
            110,
            Games.Status.FINAL,
            Instant.parse("2026-04-18T02:00:00Z"));
        test.setId(id);
        when(gamesRepo.findById(id)).thenThrow(new IllegalArgumentException("Wrong Fields For Alert."));
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            alertsService.createAlert(test, "Phoenix Suns", Alerts.AlertType.SCORE_OVER, 120);
        });
        assertEquals("Wrong Fields For Alert.", exception.getMessage());
        verify(alertsRepo, times(0)).save(any(Alerts.class));
        verify(gamesRepo).findById(id);
    }
    // old getAllAlerts
    // @Test 
    // public void getAllAlertsTest()
    // {
    //     Alerts a1 = buildAlerts();
    //     Alerts a2 = new Alerts(a1.getGame(), "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 100);
    //     when(alertsRepo.findAll()).thenReturn(List.of(a1, a2));
    //     List<Alerts> listAleerts = alertsService.getAllAlerts();

    //     assertEquals(2, listAleerts.size());
    //     assertEquals(a1, listAleerts.get(0));
    //     assertEquals(a2, listAleerts.get(1));
    // }
    // testing some ways a query can be ran w Specifications
    @Test
    public void getAlertsSpecifications()
    {
        Alerts a1 = buildAlerts();
        Alerts a2 = new Alerts(a1.getGame(), "Golden State Warriors", Alerts.AlertType.SCORE_OVER, 100);
        when(alertsRepo.findAll(any(Specification.class))).thenReturn(List.of(a1, a2));
        List<Alerts> listAlerts = alertsService.getAllAlerts(null, null, null, null);

        assertEquals(2, listAlerts.size());
        assertEquals(a1, listAlerts.get(0));
        assertEquals(a2, listAlerts.get(1));
    }
    @Test
    public void getAlertsSpecificationsTest2()
    {
        Alerts a1 = buildAlerts();
        a1.setAlertStatus(Alerts.AlertStatus.TRIGGERED);
        Alerts a2 = new Alerts(a1.getGame(), "Golden State Warriors", Alerts.AlertType.SCORE_UNDER, 100);
        a2.setAlertStatus(Alerts.AlertStatus.TRIGGERED);

        when(alertsRepo.findAll(any(Specification.class))).thenReturn(List.of(a1, a2)).thenReturn(List.of(a1, a2)).thenReturn(List.of(a2)).thenReturn(List.of(a1));

        // testing instant / date query
        List<Alerts> listAlertsDate = alertsService.getAllAlerts(null, null, null, Instant.parse("2026-04-18T02:00:00Z"));
        assertEquals(2, listAlertsDate.size());
        assertEquals(a1, listAlertsDate.get(0));
        assertEquals(a2, listAlertsDate.get(1));
        // testing status query 
        List<Alerts> listAlertsStatus = alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, null, null, null);
        assertEquals(2, listAlertsStatus.size());
        assertEquals(a1, listAlertsStatus.get(0));
        assertEquals(a2, listAlertsStatus.get(1));
        // testing status + type query
        List<Alerts> listAlertsUnder = alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_UNDER, null, null);
        assertEquals(1, listAlertsUnder.size());
        assertEquals(a2, listAlertsUnder.get(0));
        List<Alerts> listAlertsOver = alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_OVER, null, null);
        assertEquals(1, listAlertsUnder.size());
        assertEquals(a1, listAlertsOver.get(0));        
        // testing status + type + team query
        List<Alerts> listsAlertsMix = alertsService.getAllAlerts(Alerts.AlertStatus.TRIGGERED, Alerts.AlertType.SCORE_OVER, "Phoenix Suns", null);
        assertEquals(1, listsAlertsMix.size());
        assertEquals(a1, listsAlertsMix.get(0));
    }
    @Test
    public void getAlertsByIdTest1()
    {
        Long id = 1L;
        Alerts a1 = buildAlerts();
        when(alertsRepo.findById(id)).thenReturn(Optional.of(a1));
        Alerts a2 = alertsService.getAlertById(id);
        assertNotNull(a2);
        assertEquals(a1, a2);
    }
    @Test
    public void getAlertsByIdTest2()
    {
        Long id = 2L;
        when(alertsRepo.findById(id)).thenThrow(new EntityNotFoundException("Alert ID not found."));
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            alertsService.getAlertById(id);
        });
        assertEquals("Alert ID not found.", exception.getMessage());
    }
    @Test
    public void getAlertsByTeamNameTest()
    {
        Alerts a1 = buildAlerts();
        String team = "Phoenix Suns";
        when(alertsRepo.findByTeamName(team)).thenReturn(List.of(a1));
        List<Alerts> list = alertsService.getAlertsByTeamName(team);
        assertEquals(1, list.size());
        assertEquals(team, list.get(0).getTeamName());
        assertEquals(a1, list.get(0));
    }
    @Test 
    public void getAlertsByAlertType()
    {
        Alerts a1 = buildAlerts();
        Alerts.AlertType type = Alerts.AlertType.SCORE_OVER;
        when(alertsRepo.findByAlertType(type)).thenReturn(List.of(a1));
        List<Alerts> list = alertsService.getAlertsByAlertType(type);
        assertEquals(1, list.size());
        assertEquals(type, list.get(0).getAlertType());
        assertEquals(a1, list.get(0));
    }
    @Test
    public void getAlertsByAlertStatus()
    {
        Alerts a1 = buildAlerts();
        Alerts.AlertStatus stat = Alerts.AlertStatus.CREATED;
        when(alertsRepo.findByStatus(stat)).thenReturn(List.of(a1));
        List <Alerts> list = alertsService.getAlertsByStatus(stat);
        assertEquals(1, list.size());
        assertEquals(stat, list.get(0).getAlertStatus());
        assertEquals(a1, list.get(0));
    }
    @Test
    public void getAlertsByCreatedAtTest()
    {
        Alerts a1 = buildAlerts();
        a1.setCreatedAt(Instant.parse("2026-04-18T02:00:00Z"));
        Instant time = Instant.parse("2026-04-18T02:00:00Z");
        when(alertsRepo.findByCreatedAt(time)).thenReturn(List.of(a1));
        List<Alerts> list = alertsService.getAlertsByCreatedAt(time);
        assertEquals(1, list.size());
        assertEquals(time, list.get(0).getCreatedAt());
        assertEquals(a1, list.get(0));
    }
    @Test
    public void getAlertsByTypeAndTeam()
    {
        Alerts a1 = buildAlerts();
        String team = "Phoenix Suns";
        Alerts.AlertType type = Alerts.AlertType.SCORE_OVER;
        when(alertsRepo.findByTeamNameAndAlertType(team, type)).thenReturn(List.of(a1));
        List<Alerts> list = alertsService.getAlertsByTypeAndTeam(team, type);
        assertEquals(1, list.size());
        assertEquals(team, list.get(0).getTeamName());
        assertEquals(type, list.get(0).getAlertType());
        assertEquals(a1, list.get(0));
    }
    @Test
    public void deleteTest() {
        Long id = 1L;

        alertsService.deleteAlert(id);

        verify(alertsRepo).deleteById(id);
    }
    @Test
    public void updateAlertsStatus_scoreOver_triggersTest1() {
        Alerts.AlertStatus stat = Alerts.AlertStatus.CREATED;
        Games game = new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,   
            110,   
            Games.Status.LIVE,
            Instant.now() 
        );
        Alerts alert = new Alerts(
            game,
            "Phoenix Suns",
            AlertType.SCORE_OVER,
            120
        );

        when(alertsRepo.findByStatus(stat)).thenReturn(List.of(alert));

        alertsService.updateAlertsStatus(stat);

        assertEquals(AlertStatus.TRIGGERED, alert.getAlertStatus());
    }
    @Test
    public void updateAlertsStatus_scoreOver_triggersTest2() {
        Alerts.AlertStatus stat = Alerts.AlertStatus.CREATED;
        Games game = new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,   
            132,   
            Games.Status.LIVE,
            Instant.now() 
        );
        Alerts alert = new Alerts(
            game,
            "Golden State Warriors",
            AlertType.SCORE_OVER,
            130
        );

        when(alertsRepo.findByStatus(stat)).thenReturn(List.of(alert));

        alertsService.updateAlertsStatus(stat);
        assertEquals(AlertStatus.TRIGGERED, alert.getAlertStatus());
    }
    @Test
    public void UpdatedAlertStatus_scoreUnder_triggersTest1()
    {
        Alerts.AlertStatus stat = Alerts.AlertStatus.CREATED;
        Games game = new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,   
            110,   
            Games.Status.FINAL,
            Instant.now() 
        );
        Alerts alert = new Alerts(
            game,
            "Phoenix Suns",
            AlertType.SCORE_UNDER,
            125
        );
        
        when(alertsRepo.findByStatus(stat)).thenReturn(List.of(alert));

        alertsService.updateAlertsStatus(stat);
        assertEquals(Alerts.AlertStatus.TRIGGERED, alert.getAlertStatus());
    }
    @Test
    public void UpdatedAlertStatus_scoreUnder_triggersTest2()
    {
        Alerts.AlertStatus stat = Alerts.AlertStatus.CREATED;
        Games game = new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,   
            110,   
            Games.Status.FINAL,
            Instant.now() 
        );
        Alerts alert = new Alerts(
            game,
            "Golden State Warriors",
            AlertType.SCORE_UNDER,
            115
        );
        
        when(alertsRepo.findByStatus(stat)).thenReturn(List.of(alert));
        alertsService.updateAlertsStatus(stat);
        assertEquals(Alerts.AlertStatus.TRIGGERED, alert.getAlertStatus());
    }
    @Test 
    public void UpdatedAlertStattus_Finished_Test1()
    {
        Games game = new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,   
            110,   
            Games.Status.FINAL,
            Instant.now() 
        );
        game.setUpdatedTime(Instant.now(Clock.fixed(Instant.parse("2026-04-16T22:40:00Z"), ZoneOffset.UTC)));
        Alerts alert = new Alerts(
            game,
            "Golden State Warriors",
            AlertType.SCORE_UNDER,
            115
        );
        alert.setAlertStatus(Alerts.AlertStatus.TRIGGERED);
        when(alertsRepo.findByStatus(alert.getAlertStatus())).thenReturn(List.of(alert));

        alertsService.updateAlertsStatus(alert.getAlertStatus());

        assertEquals(Alerts.AlertStatus.FINISHED, alert.getAlertStatus());
    }
    @Test 
    public void UpdatedAlertStattus_Finished_Test2()
    {
        Games game = new Games(
            "401866759",
            "Phoenix Suns",
            "Golden State Warriors",
            121,   
            110,   
            Games.Status.FINAL,
            Instant.now() 
        );
        game.setUpdatedTime(Instant.now(Clock.fixed(Instant.parse("2026-04-16T22:40:00Z"), ZoneOffset.UTC)));
        Alerts alert = new Alerts(
            game,
            "Golden State Warriors",
            AlertType.SCORE_UNDER,
            109
        );
        when(alertsRepo.findByStatus(alert.getAlertStatus())).thenReturn(List.of(alert));

        alertsService.updateAlertsStatus(alert.getAlertStatus());

        assertEquals(Alerts.AlertStatus.FINISHED, alert.getAlertStatus());
    }
}
