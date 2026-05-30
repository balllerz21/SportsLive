package org.example.sportslivev1.integrationTests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.sportslivev1.DemoApplication;
import org.example.sportslivev1.controller.AlertsController;
import org.example.sportslivev1.dto.AlertsRequest;
import org.example.sportslivev1.entity.Alerts;
import org.example.sportslivev1.entity.Alerts.AlertStatus;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.repository.UsersRepo;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.cert.PKIXRevocationChecker.Option;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.everyItem;

import jakarta.servlet.ServletContext;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { DemoApplication.class })
@WebAppConfiguration
// DONE: Fix this layout as users should not be created to check alerts logic.

public class AlertsIntregationTests {
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
        assertNotNull(webApplicationContext.getBean(AlertsController.class));
    }
    @Test 
    public void getAllAlertsTest() throws Exception
    {
        this.mockMvc.perform(get("/alerts")
        .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].teamName").exists())
        .andExpect(jsonPath("$[*].alertType").exists())
        .andExpect(jsonPath("$[*].status").exists());;
    }
    @Test
    public void getAllAlertsWithParams() throws Exception
    {
        this.mockMvc.perform(get("/alerts")
        .param("teamName", "Charlotte Hornets")
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].teamName").exists())
        .andExpect(jsonPath("$[*].alertType").exists())
        .andExpect(jsonPath("$[*].status").exists())
        .andExpect(jsonPath("$[*].teamName").value(everyItem(Matchers.containsString("Charlotte Hornets"))));
    }
    @Test
    public void getAllAlertsWithParamsTest2() throws Exception
    {
        this.mockMvc.perform(get("/alerts")
        .param("status","FINISHED")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].teamName").exists())
        .andExpect(jsonPath("$[*].alertType").exists())
        .andExpect(jsonPath("$[*].status").exists())
        .andExpect(jsonPath("$[*].status").value(everyItem(Matchers.containsString("FINISHED"))));
    }
    @Test
    public void getAllAlertsWithParamsTest3() throws Exception
    {
        this.mockMvc.perform(get("/alerts")
        .param("alertType","SCORE_OVER")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].teamName").exists())
        .andExpect(jsonPath("$[*].alertType").exists())
        .andExpect(jsonPath("$[*].status").exists())
        .andExpect(jsonPath("$[*].alertType").value(everyItem(Matchers.containsString("SCORE_OVER"))));
    }
    @Test
    public void getAlertByIdExists() throws Exception
    {
        this.mockMvc.perform(get("/alerts/{id}", "303")
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("303"))
        .andExpect(jsonPath("$.teamName").value("Charlotte Hornets"))
        .andExpect(jsonPath("$.alertType").value("SCORE_OVER"))
        .andExpect(jsonPath("$.status").value("FINISHED"));
    }
    @Test
    public void getAlertByIdDoesNotExist() throws Exception
    {
        this.mockMvc.perform(get("/alerts/{id}", "1000")
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().is(404));
    }
    @Test 
    public void createAlertTest() throws Exception
    {
        // making the request body
        AlertsRequest res = new AlertsRequest();
        res.setGameId(1555l);
        res.setTeamName("San Antonio Spurs");
        res.setAlertType(Alerts.AlertType.SCORE_OVER);
        res.setTargetVal(100);

        // turning it into json
        String json  = new ObjectMapper().writeValueAsString(res);
    
        this.mockMvc.perform(post("/alerts/add")
        // using admin user here
        .principal(new UsernamePasswordAuthenticationToken("balllerz21", null))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
    }
    @Test
    public void createAlertTestGameIdNotFound() throws Exception
    {
        // making the request body
        AlertsRequest res = new AlertsRequest();
        res.setGameId(1l);
        res.setTeamName("San Antonio Spurs");
        res.setAlertType(Alerts.AlertType.SCORE_OVER);
        res.setTargetVal(100);

        // turning it into json
        String json  = new ObjectMapper().writeValueAsString(res);
        this.mockMvc.perform(post("/alerts/add")
        .principal(new UsernamePasswordAuthenticationToken("testuser", null))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnprocessableContent());
    }

}
