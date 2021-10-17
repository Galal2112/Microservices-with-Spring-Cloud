package htwb.ai.lyricsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import htwb.ai.lyricsservice.common.Lyrics;
import htwb.ai.lyricsservice.common.LyricsResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class MusixmatchService {
    private static final String BASE_URL = "https://api.musixmatch.com/ws/1.1";

    @Autowired
    private final Environment env;
    private final RestTemplate template;

    public Lyrics getLyrics(String songTitle, String artist) throws JsonProcessingException {
        String apiKey = env.getProperty("API_KEY");
        String apiUrl = BASE_URL + "/matcher.lyrics.get?q_track="
                + URLEncoder.encode(songTitle, StandardCharsets.UTF_8)
                + ((artist != null && !artist.isEmpty()) ? "&q_artist=" + URLEncoder.encode(artist, StandardCharsets.UTF_8) : "")
                + "&apikey=" + apiKey;
        String responseBody = template.getForObject(URI.create(apiUrl), String.class);
        JsonMapper jsonMapper = new JsonMapper();
        LyricsResponse lyricsResponse = jsonMapper.readValue(responseBody, LyricsResponse.class);
       return lyricsResponse.message.body.lyrics;
    }
}
