package org.example.sportslivev1.service;

import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.Users;

public interface UsersService {
    Users createUser(String userName, String passwordHash, Users.UserRole role);
    Users getUserById(Long id);
    Users getUserByUserName(String userName);
    List<Users> getAllUsers();
    void deleteUser(Long id);
} 
