package org.example.sportslivev1.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class EspnClientAPI {
    private final RestClient espn;
    public EspnClientAPI()
    {
        this.espn = RestClient.builder()
                .baseUrl("https://site.api.espn.com")
                .build();
    }
    public String getScoreBoard()
    {
        return this.espn.get().uri("/apis/site/v2/sports/basketball/nba/scoreboard").retrieve().body(String.class);
    }
}
