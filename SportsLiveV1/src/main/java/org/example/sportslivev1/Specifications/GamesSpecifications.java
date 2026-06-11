package org.example.sportslivev1.specifications;
import org.springframework.data.jpa.domain.Specification;
import org.example.sportslivev1.entity.Games;

public class GamesSpecifications {
    public static Specification<Games> hasStatus(Games.Status status)
    {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }
}
