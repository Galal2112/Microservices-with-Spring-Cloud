package htwb.ai.authservice.service;

import htwb.ai.authservice.entity.User;
import htwb.ai.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void authenticateSuccess() {
        String userId = "mmuster";
        String password = "123456";
        User user = new User();
        user.setUserId(userId);
        user.setPassword(password);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        String token = userService.authenticate(userId, password);
        assertNotNull(token);
        verify(userRepository).save(user);
    }

    @Test
    void authenticateWithoutUserIdNoAuthToken() {
        String authToken = userService.authenticate(null,"1235");
        assertNull(authToken);
    }

    @Test
    void authenticateWithoutPasswordNoAuthToken() {
        String authToken = userService.authenticate("test",null);
        assertNull(authToken);
    }

    @Test
    void authenticateWithoutDataNoAuthToken() {
        String authToken = userService.authenticate(null,null);
        assertNull(authToken);
    }

    @Test
    void authenticateWithNonRegisteredUserNoAuthToken() {
        String userId = "mmuster";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        String authToken = userService.authenticate(userId,"123456");
        assertNull(authToken);
    }

    @Test
    void authenticateWithWrongPasswordNoAuthToken() {
        String userId = "mmuster";
        User user = new User();
        user.setUserId(userId);
        user.setPassword("123456");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        String authToken = userService.authenticate(userId,"WrongPW");
        assertNull(authToken);
        verify(userRepository, never()).save(user);
    }

    @Test
    void findUserByAuthTokenSuccess() {
        String token = "122215615689451";
        User user = new User();
        user.setAuthToken(token);
        when(userRepository.findUserByAuthToken(token)).thenReturn(Optional.of(user));
        Optional<User> userValue = userService.findUserByAuthToken(token);
        assertTrue(userValue.isPresent());
        assertEquals(token, userValue.get().getAuthToken());
    }

    @Test
    void findUserByUserIdSuccess() {
        String userId = "mmuster";
        User user = new User();
        user.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Optional<User> userValue = userService.findUserByUserId(userId);
        assertTrue(userValue.isPresent());
        assertEquals(userId, userValue.get().getUserId());
    }
}