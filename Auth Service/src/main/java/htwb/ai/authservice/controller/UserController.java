package htwb.ai.authservice.controller;

import htwb.ai.authservice.common.LoginForm;
import htwb.ai.authservice.entity.User;
import htwb.ai.authservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/auth")
@AllArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping(consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> login(@RequestBody LoginForm loginForm) {

        String authToken = service.authenticate(loginForm.getUserId(), loginForm.getPassword());
        if (authToken == null) {
            return new ResponseEntity<>("User kann nicht authentifiziert werden",
                    HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(authToken, HttpStatus.OK);
    }

    @GetMapping
    public User getUserByAuthToken(@RequestParam(name = "auth_token") String authToken) {
        Optional<User> userOptional = service.findUserByAuthToken(authToken);
        return userOptional.orElse(null);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserByUserId(@RequestHeader("Authorization") String authorization, @PathVariable String userId) {
        if (authorization == null || authorization.isEmpty()
                || service.findUserByAuthToken(authorization).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<User> userOptional = service.findUserByUserId(userId);
        if (userOptional.isPresent()) {
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

