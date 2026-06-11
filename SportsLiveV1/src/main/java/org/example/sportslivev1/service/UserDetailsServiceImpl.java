package org.example.sportslivev1.service;

import org.example.sportslivev1.entity.SecureUsers;
import org.example.sportslivev1.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UsersRepo repository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        return repository.findByUserName(username)
                .map(SecureUsers::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
