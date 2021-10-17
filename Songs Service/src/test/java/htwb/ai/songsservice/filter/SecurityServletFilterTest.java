package htwb.ai.songsservice.filter;

import htwb.ai.songsservice.common.User;
import htwb.ai.songsservice.controller.SongController;
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
    private SongController mockController;
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        mockController = Mockito.mock(SongController.class);
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
        mockMvc.perform(get("/songs/" + songId)
                .header("Authorization", authToken))
                .andExpect(status().isUnauthorized());
        verify(mockController, never()).getSongById(songId);
    }

    @Test
    void authorizedSuccess() throws Exception {
        String authToken = "test";
        User u = new User();
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth?auth_token=" + authToken, User.class))
                .thenReturn(u);
        int songId = 1;
        when(mockController.getSongById(songId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        mockMvc.perform(get("/songs/" + songId)
                .header("Authorization", authToken))
                .andExpect(status().isOk());
        verify(mockController).getSongById(songId);
    }
}