package org.example.sportslivev1.Specifications;
import org.example.sportslivev1.entity.Alerts;
import org.springframework.data.jpa.domain.Specification;

public class AlertsSpecifications {
    // user can search by status 
    public static Specification<Alerts> hasStatus(Alerts.AlertStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }
    // user can search by team
    public static Specification<Alerts> hasTeam(String team) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("teamName"), team);
    }
    // user can search by game
    public static Specification<Alerts> hasGameId(Long gameId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("gameId"), gameId);
    }
    // user can search by type
    public static Specification<Alerts> hasType(Alerts.AlertType type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("alertTyoe"), type);
    }
}
