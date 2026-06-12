package org.example.sportslivev1.service;

import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.entity.Users.UserRole;
import org.example.sportslivev1.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersRepo usersRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Override
    public Users createUser(String userName, String passwordHash, Users.UserRole role) {
        Users user = new Users(userName, passwordEncoder.encode(passwordHash), UserRole.USER);
        return usersRepository.save(user);
    }

    @Override
    public Users getUserById(Long id) {
        Optional<Users> userOpt = usersRepository.findById(id);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            throw new IllegalArgumentException("User ID not found");
        }
    }

    @Override
    public Users getUserByUserName(String userName) throws UsernameNotFoundException{
        Optional<Users> userOpt = usersRepository.findByUserName(userName);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + userName);
        }
    }

    @Override
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        usersRepository.deleteById(id);
    }
}
