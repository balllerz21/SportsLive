package org.example.sportslivev1.integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Games;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.repository.AlertsRepo;
import org.example.sportslivev1.repository.GamesRepo;
import org.example.sportslivev1.repository.UsersRepo;
import org.example.sportslivev1.service.AlertsServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlertsPaginationIntegrationTest {
    @Autowired
    private AlertsServiceImp alertsService;

    @Autowired
    private AlertsRepo alertsRepo;

    @Autowired
    private GamesRepo gamesRepo;

    @Autowired
    private UsersRepo usersRepo;

    private Games game;
    private Users user;
    private Users otherUser;

    @BeforeEach
    void setUp() {
        alertsRepo.deleteAll();
        usersRepo.deleteAll();
        gamesRepo.deleteAll();

        game = gamesRepo.save(new Games(
            "integration-game",
            "Home",
            "Away",
            0,
            0,
            Games.Status.LIVE,
            Instant.parse("2026-06-14T20:00:00Z")
        ));
        user = usersRepo.save(new Users("testuser", "password", Users.UserRole.USER));
        otherUser = usersRepo.save(new Users("otheruser", "password", Users.UserRole.USER));
    }

    @Test
    void paginatesAfterOwnershipFilteringAndUsesServerSort() {
        Alerts triggeredOlder = saveAlert(user, Alerts.AlertStatus.TRIGGERED);
        Alerts triggeredNewer = saveAlert(user, Alerts.AlertStatus.TRIGGERED);
        Alerts created = saveAlert(user, Alerts.AlertStatus.CREATED);
        Alerts finished = saveAlert(user, Alerts.AlertStatus.FINISHED);
        saveAlert(otherUser, Alerts.AlertStatus.TRIGGERED);

        PageRequest clientPage = PageRequest.of(
            0,
            2,
            Sort.by(Sort.Direction.ASC, "teamName")
        );

        Page<Alerts> firstPage = alertsService.getAllAlerts(
            null, null, null, null, user.getUserName(), clientPage);
        Page<Alerts> secondPage = alertsService.getAllAlerts(
            null, null, null, null, user.getUserName(), clientPage.next());

        assertEquals(4, firstPage.getTotalElements());
        assertEquals(
            List.of(triggeredNewer.getId(), triggeredOlder.getId()),
            firstPage.getContent().stream().map(Alerts::getId).toList()
        );
        assertEquals(
            List.of(created.getId(), finished.getId()),
            secondPage.getContent().stream().map(Alerts::getId).toList()
        );
    }

    private Alerts saveAlert(Users owner, Alerts.AlertStatus status) {
        Alerts alert = new Alerts(game, "Home", Alerts.AlertType.SCORE_OVER, 100);
        alert.setUser(owner);
        alert.setAlertStatus(status);
        return alertsRepo.saveAndFlush(alert);
    }
}
