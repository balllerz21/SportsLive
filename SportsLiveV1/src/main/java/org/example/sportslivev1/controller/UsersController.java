package org.example.sportslivev1.controller;

import java.util.stream.Collectors;

import org.example.sportslivev1.auth.JwtResponse;
import org.example.sportslivev1.dto.UserRequest;
import org.example.sportslivev1.dto.UsersMapper;
import org.example.sportslivev1.entity.SecureUsers;
import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.example.sportslivev1.utils.JwtUtilities;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersServiceImpl usersService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtilities jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> logIn(@RequestBody UserRequest entity) {
        try {
        // get authentication token and return it to the user.
        Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(entity.getUsername(), entity.getPasswordHash()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // get user details and roles to return to the user along with the token.
        String jwt = jwtUtils.generateJwtToken(authentication); 
        SecureUsers principal = (SecureUsers) authentication.getPrincipal();
        List<String> roles = principal.getAuthorities().stream().map(item -> item.getAuthority())
        .collect(Collectors.toList());
        // return the token and user details to the user.
        return ResponseEntity.ok(new JwtResponse(principal.getId(), jwt, principal.getUsername(), roles));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Error: Invalid username or password");
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserRequest entity) {
        try {
            if (usersService.getUserByUserName(entity.getUsername()) != null) {
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }
        } catch (UsernameNotFoundException ex) {
            // Username is available, so continue with signup.
        }
        Users newUser = usersService.createUser(entity.getUsername(), entity.getPasswordHash(), entity.getRole());
        return ResponseEntity.ok(UsersMapper.toUserResponse(newUser));
    }
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Users user;
        try {
            user = usersService.getUserById(id);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UsersMapper.toUserResponse(user));
    }
    
    

}
