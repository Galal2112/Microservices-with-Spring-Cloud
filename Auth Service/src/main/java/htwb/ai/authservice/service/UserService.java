package htwb.ai.authservice.service;

import htwb.ai.authservice.entity.User;
import htwb.ai.authservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repository;

    public String authenticate(String userId, String password) {
        if (userId == null || password == null || userId.isEmpty() || password.isEmpty()) {
            return null;
        }
        Optional<User> optionalUser = repository.findById(userId);
        if (optionalUser.isEmpty()) {
            return null;
        }
        User user = optionalUser.get();
        if (user.getUserId() == null ||
                user.getPassword() == null) {
            return null;
        }

        if (user.getUserId().equals(userId) && user.getPassword().equals(password)) {
            String authToken = UUID.randomUUID().toString();
            user.setAuthToken(authToken);
            repository.save(user);
            return authToken;
        }

        return null;
    }

    public Optional<User> findUserByAuthToken(String token) {
        return repository.findUserByAuthToken(token);
    }

    public Optional<User> findUserByUserId(String userId) {
        return repository.findById(userId);
    }
}
