package htwb.ai.lyricsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import htwb.ai.lyricsservice.common.Lyrics;
import htwb.ai.lyricsservice.common.LyricsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class MusixmatchServiceTest {

    private MusixmatchService service;
    private RestTemplate restTemplate;
    private final String apiKey = "apiKey";

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        Environment env = Mockito.mock(Environment.class);
        when(env.getProperty("API_KEY")).thenReturn(apiKey);
        service = new MusixmatchService(env, restTemplate);
    }

    @Test
    void getLyricsWithNullArtist() throws JsonProcessingException {
        String responseBody = lyricsResponse();
        JsonMapper jsonMapper = new JsonMapper();
        LyricsResponse expectedResponse = jsonMapper.readValue(responseBody, LyricsResponse.class);
        String title = "Chinese Food";
        when(restTemplate.getForObject(URI.create("https://api.musixmatch.com/ws/1.1/matcher.lyrics.get?q_track="
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&apikey=" + apiKey), String.class)).thenReturn(responseBody);
        Lyrics lyrics = service.getLyrics(title, null);
        assertEquals(expectedResponse.getLyrics().lyricsBody, lyrics.lyricsBody);
    }

    @Test
    void getLyricsWithWithArtist() throws JsonProcessingException {
        String responseBody = lyricsResponse();
        JsonMapper jsonMapper = new JsonMapper();
        LyricsResponse expectedResponse = jsonMapper.readValue(responseBody, LyricsResponse.class);
        String title = "Chinese Food";
        String artist = "Alison Gold";
        when(restTemplate.getForObject(URI.create("https://api.musixmatch.com/ws/1.1/matcher.lyrics.get?q_track="
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&q_artist=" + URLEncoder.encode(artist, StandardCharsets.UTF_8)
                + "&apikey=" + apiKey), String.class)).thenReturn(responseBody);
        Lyrics lyrics = service.getLyrics(title, artist);
        assertEquals(expectedResponse.message.body.lyrics.lyricsBody, lyrics.lyricsBody);
    }

    private String lyricsResponse() {
        return "{\n" +
                "  \"message\": {\n" +
                "    \"header\": {\n" +
                "      \"status_code\": 200,\n" +
                "      \"execute_time\": 0.10864496231079\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "      \"lyrics\": {\n" +
                "        \"lyrics_id\": 8575206,\n" +
                "        \"explicit\": 0,\n" +
                "        \"lyrics_body\": \"Abc\",\n" +
                "        \"script_tracking_url\": \"tracking_url\",\n" +
                "        \"pixel_tracking_url\": \"tracking_url\",\n" +
                "        \"lyrics_copyright\": \"Lyrics powered by www.musixmatch.com.\",\n" +
                "        \"updated_time\": \"2017-07-25T18:34:49Z\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}