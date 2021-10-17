package htwb.ai.lyricsservice;

import htwb.ai.lyricsservice.controller.LyricsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class LyricsServiceApplicationTests {

    @Autowired
    private LyricsController controller;

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }
}
