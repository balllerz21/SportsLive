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
    public String getScoreBoard(String sport, String site)
    {
        return this.espn.get().uri("/apis/site/v2/sports/{sport}/{site}/scoreboard", sport, site).retrieve().body(String.class);
    }
}
