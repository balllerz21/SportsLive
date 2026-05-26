package org.example.sportslivev1.serviceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.example.sportslivev1.entity.Users;
import org.example.sportslivev1.entity.Users.UserRole;
import org.example.sportslivev1.repository.UsersRepo;
import org.example.sportslivev1.service.UsersServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {
    @Mock
    private UsersRepo usersRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersServiceImpl usersService;

    private Users buildUser() {
        Users user = new Users("testuser", "encoded-password", UserRole.USER);
        user.setId(1L);
        return user;
    }

    @Test
    public void createUserTest() {
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        usersService.createUser("testuser", "plain-password", UserRole.USER);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepo).save(captor.capture());

        Users savedUser = captor.getValue();
        assertEquals("testuser", savedUser.getUserName());
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertEquals(UserRole.USER, savedUser.getRole());
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    public void getUserByIdTest1() {
        Long id = 1L;
        Users user = buildUser();

        when(usersRepo.findById(id)).thenReturn(Optional.of(user));

        Users result = usersService.getUserById(id);

        assertNotNull(result);
        assertSame(user, result);
    }

    @Test
    public void getUserByIdTest2() {
        Long id = 99L;

        when(usersRepo.findById(id)).thenReturn(Optional.empty());

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            usersService.getUserById(id);
        });

        assertEquals("User ID not found", exception.getMessage());
    }

    @Test
    public void getUserByUserNameTest1() {
        Users user = buildUser();

        when(usersRepo.findByUserName("testuser")).thenReturn(Optional.of(user));

        Users result = usersService.getUserByUserName("testuser");

        assertNotNull(result);
        assertSame(user, result);
    }

    @Test
    public void getUserByUserNameTest2() {
        when(usersRepo.findByUserName("missinguser")).thenReturn(Optional.empty());

        Throwable exception = assertThrows(UsernameNotFoundException.class, () -> {
            usersService.getUserByUserName("missinguser");
        });

        assertEquals("User not found with username: missinguser", exception.getMessage());
    }

    @Test
    public void getAllUsersTest() {
        Users user1 = buildUser();
        Users user2 = new Users("adminuser", "admin-password", UserRole.ADMIN);
        user2.setId(2L);

        when(usersRepo.findAll()).thenReturn(List.of(user1, user2));

        List<Users> result = usersService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
    }

    @Test
    public void deleteUserTest() {
        Long id = 1L;

        usersService.deleteUser(id);

        verify(usersRepo).deleteById(id);
    }
}
