package org.example.sportslivev1.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class SecureUsers implements UserDetails{
    @Autowired
    private Users user;

    public SecureUsers(Users u) {
        this.user = u;
    }

    @Override
    public String getUsername() {
        return user.getUserName();  
    }
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Users.UserRole temp = user.getRole();
        String role = temp.name();
        String prefixedRole =  role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(prefixedRole));
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;    
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    
}
