package org.example.sportslivev1.controllerTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


import org.example.sportslivev1.auth.AuthEntryPointJwt;
import org.example.sportslivev1.auth.AuthTokenFilter;
import org.example.sportslivev1.controller.UsersController;
import org.example.sportslivev1.dto.UserRequest;
import org.example.sportslivev1.entity.SecureUsers;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.entity.Users.UserRole;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.example.sportslivev1.utils.JwtUtilities;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsersServiceImpl usersService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtilities jwtUtilities;

    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @MockitoBean
    private AuthEntryPointJwt authEntryPointJwt;

    private UserRequest buildRequest(String username, String password, UserRole role) {
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setPasswordHash(password);
        request.setRole(role);
        return request;
    }

    @Test
    public void signUpTest() throws Exception {
        UserRequest request = buildRequest("newuser", "plain-password", UserRole.USER);
        Users savedUser = new Users("newuser", "encoded-password", UserRole.USER);
        savedUser.setId(1L);

        when(usersService.getUserByUserName("newuser"))
            .thenThrow(new UsernameNotFoundException("User not found with username: newuser"));
        when(usersService.createUser("newuser", "plain-password", UserRole.USER)).thenReturn(savedUser);

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.alerts").isArray())
                .andExpect(jsonPath("$.alerts").isEmpty());

        verify(usersService).getUserByUserName("newuser");
        verify(usersService).createUser("newuser", "plain-password", UserRole.USER);
    }

    @Test
    public void signUpDuplicateUserTest() throws Exception {
        UserRequest request = buildRequest("existinguser", "plain-password", UserRole.USER);
        Users existingUser = new Users("existinguser", "encoded-password", UserRole.USER);

        when(usersService.getUserByUserName("existinguser")).thenReturn(existingUser);

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Username is already taken!"));

        verify(usersService).getUserByUserName("existinguser");
        verify(usersService, never()).createUser(any(String.class), any(String.class), any(UserRole.class));
    }

    @Test
    public void logInTest() throws Exception {
        UserRequest request = buildRequest("testuser", "plain-password", UserRole.USER);
        Users user = new Users("testuser", "encoded-password", UserRole.USER);
        user.setId(22L);
        SecureUsers principal = new SecureUsers(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtilities.generateJwtToken(authentication)).thenReturn("jwt-token");

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.id").value(22L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertLoginToken(captor.getValue());
        verify(jwtUtilities).generateJwtToken(authentication);
    }
    @Test
    public void logInTestFailed() throws Exception {
        UserRequest request = buildRequest("testuser", "wrong-password", UserRole.USER);

        doThrow(new BadCredentialsException("Invalid username or password"))
                .when(authenticationManager)
                .authenticate(any(Authentication.class));

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Error: Invalid username or password"));
    }

    @Test
    public void getProfileTest() throws Exception {
        Users user = new Users("profileuser", "encoded-password", UserRole.ADMIN);
        user.setId(7L);

        when(usersService.getUserById(7L)).thenReturn(user);

        mockMvc.perform(get("/users/profile/7")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profileuser"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.alerts").isArray())
                .andExpect(jsonPath("$.alerts").isEmpty());

        verify(usersService).getUserById(7L);
    }

    @Test
    public void getProfileNotFoundTest() throws Exception {
        when(usersService.getUserById(99L)).thenThrow(new IllegalArgumentException("User ID not found"));

        mockMvc.perform(get("/users/profile/99")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(usersService).getUserById(99L);
    }

    private void assertLoginToken(UsernamePasswordAuthenticationToken token) {
        org.junit.jupiter.api.Assertions.assertEquals("testuser", token.getPrincipal());
        org.junit.jupiter.api.Assertions.assertEquals("plain-password", token.getCredentials());
    }
}
