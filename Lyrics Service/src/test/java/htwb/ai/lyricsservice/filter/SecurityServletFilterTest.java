package htwb.ai.lyricsservice.filter;

import htwb.ai.lyricsservice.common.User;
import htwb.ai.lyricsservice.controller.LyricsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityServletFilterTest {
    private MockMvc mockMvc;
    private LyricsController mockController;
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        mockController = Mockito.mock(LyricsController.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        mockMvc = MockMvcBuilders.standaloneSetup(mockController)
                .addFilters(new SecurityServletFilter(restTemplate))
                .build();
    }

    @Test
    void unauthorizedCallShouldNotAccessController() throws Exception {
        String authToken = "test";
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth?auth_token=" + authToken, User.class))
                .thenReturn(null);
        int songId = 1;
        mockMvc.perform(get("/lyrics?songId=" + songId)
                .header("Authorization", authToken))
                .andExpect(status().isUnauthorized());
        verify(mockController, never()).getLyrics(authToken, songId);
    }

    @Test
    void authorizedSuccess() throws Exception {
        String authToken = "test";
        User u = new User();
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth?auth_token=" + authToken, User.class))
                .thenReturn(u);
        int songId = 1;
        when(mockController.getLyrics(authToken, songId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        mockMvc.perform(get("/lyrics?songId=" + songId)
                .header("Authorization", authToken))
                .andExpect(status().isOk());
        verify(mockController).getLyrics(authToken, songId);
    }
}