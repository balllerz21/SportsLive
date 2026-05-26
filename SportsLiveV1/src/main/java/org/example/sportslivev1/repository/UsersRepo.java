package org.example.sportslivev1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.example.sportslivev1.entity.Users;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {
    public Optional<Users> findByUserName(String userName);
    public Optional<Users> findById(Long id);
} 
