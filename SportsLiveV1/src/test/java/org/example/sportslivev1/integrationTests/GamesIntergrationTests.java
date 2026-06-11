package org.example.sportslivev1.integrationTests;

import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.sportslivev1.DemoApplication;
import org.example.sportslivev1.controller.GamesController;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import jakarta.servlet.ServletContext;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DemoApplication.class })
@WebAppConfiguration

public class GamesIntergrationTests {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }
    @Test
    public void givenWac_whenServletContext_thenItProvidesAlertController() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        
        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean(GamesController.class));
    }
    @Test
    public void getAllGamesTest() throws Exception
    {
        this.mockMvc.perform(get("/games").accept(MediaType.APPLICATION_JSON)).andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].actualGameId").exists())
        .andExpect(jsonPath("$[*].homeTeam").exists())
        .andExpect(jsonPath("$[*].awayTeam").exists())
        .andExpect(jsonPath("$[*].homeScore").exists())
        .andExpect(jsonPath("[*].status").exists());
    }
    @Test void getAllGamesWStatusParam() throws Exception
    {
        this.mockMvc.perform(get("/games").param("status", "FINAL").accept(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].actualGameId").exists())
        .andExpect(jsonPath("$[*].homeTeam").exists())
        .andExpect(jsonPath("$[*].awayTeam").exists())
        .andExpect(jsonPath("$[*].homeScore").exists())
        .andExpect(jsonPath("[*].status").exists())
        .andExpect(jsonPath("[*].status").value(everyItem(Matchers.containsString("FINAL"))));
    }
    // this tests when games are live since this is more based on actual times where the games are actual live it will fail if no game is registered as live
    // @Test void getAllGamesWStatusParamTest2() throws Exception
    // {
    //     this.mockMvc.perform(get("/games").param("status", "LIVE").accept(MediaType.APPLICATION_JSON))
    //     .andDo(print()).andExpect(jsonPath("$").isArray())
    //     .andExpect(jsonPath("$[*].id").exists())
    //     .andExpect(jsonPath("$[*].actualGameId").exists())
    //     .andExpect(jsonPath("$[*].homeTeam").exists())
    //     .andExpect(jsonPath("$[*].awayTeam").exists())
    //     .andExpect(jsonPath("$[*].homeScore").exists())
    //     .andExpect(jsonPath("[*].status").exists())
    //     .andExpect(jsonPath("[*].status").value(everyItem(Matchers.containsString("LIVE"))));
    // }
    // this tests when games are scheduled since this is more based on actual times where the games are scheduled it will fail if no game is registered as scheduled
    // @Test void getAllGamesWStatusParamTest3() throws Exception
    // {
    //     this.mockMvc.perform(get("/games").param("status", "SCHEDULED").accept(MediaType.APPLICATION_JSON))
    //     .andDo(print()).andExpect(jsonPath("$").isArray())
    //     .andExpect(jsonPath("$[*].id").exists())
    //     .andExpect(jsonPath("$[*].actualGameId").exists())
    //     .andExpect(jsonPath("$[*].homeTeam").exists())
    //     .andExpect(jsonPath("$[*].awayTeam").exists())
    //     .andExpect(jsonPath("$[*].homeScore").exists())
    //     .andExpect(jsonPath("[*].status").exists())
    //     .andExpect(jsonPath("[*].status").value(everyItem(Matchers.containsString("SCHEDULED"))));
    // }
    @Test
    public void getGamesById() throws Exception
    {
        this.mockMvc.perform(get("/games/{id}", "1555").accept(MediaType.APPLICATION_JSON)).andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.id").value("1555"))
        .andExpect(jsonPath("$.homeTeam").exists())
        .andExpect(jsonPath("$.awayTeam").exists())
        .andExpect(jsonPath("$.homeScore").exists())
        .andExpect(jsonPath("awayScore").exists());
    }
    @Test
    public void getGamesByIdDoesNotExists() throws Exception
    {
        this.mockMvc.perform(get("/games/{id}", "100000").accept(MediaType.APPLICATION_JSON)).andDo(print())
        .andExpect(status().isNotFound());
    }
}
