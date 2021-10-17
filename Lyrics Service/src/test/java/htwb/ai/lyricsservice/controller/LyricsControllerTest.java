package htwb.ai.lyricsservice.controller;

import htwb.ai.lyricsservice.service.LyricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class LyricsControllerTest {

    private MockMvc mockMvc;
    private LyricsService lyricsService;

    @BeforeEach
    void setup() {
        lyricsService = Mockito.mock(LyricsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new LyricsController(lyricsService)).build();
    }

    @Test
    void getLyricsSuccess() throws Exception {
        String lyrics = "abcd";
        String authToken = "test";
        when(lyricsService.getLyrics(1, authToken)).thenReturn(lyrics);
        ResultActions resultActions = mockMvc.perform(get("/lyrics?songId=1")
                .header("Authorization", authToken))
                .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String lyricsResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(lyrics, lyricsResponse);
    }

    @Test
    void getLyricsForNonExistingSongShouldReturnNotFound() throws Exception {
        when(lyricsService.getLyrics(anyInt(), anyString())).thenThrow(new IllegalArgumentException());
        mockMvc.perform(get("/lyrics?songId=" + Integer.MAX_VALUE)
                .header("Authorization", "test"))
                .andExpect(status().isNotFound());
    }
}