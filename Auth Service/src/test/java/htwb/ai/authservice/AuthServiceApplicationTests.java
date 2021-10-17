package htwb.ai.authservice;

import htwb.ai.authservice.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AuthServiceApplicationTests {

    @Autowired
    private UserController controller;

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }
}
