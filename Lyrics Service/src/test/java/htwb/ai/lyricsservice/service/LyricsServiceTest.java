package htwb.ai.lyricsservice.service;

import htwb.ai.lyricsservice.common.Lyrics;
import htwb.ai.lyricsservice.common.Song;
import htwb.ai.lyricsservice.entity.SongLyrics;
import htwb.ai.lyricsservice.repository.SongLyricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class LyricsServiceTest {
    private RestTemplate restTemplate;
    private SongLyricsRepository repository;
    private LyricsService service;
    private MusixmatchService musicWebservice;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(SongLyricsRepository.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        musicWebservice = Mockito.mock(MusixmatchService.class);
        service = new LyricsService(restTemplate, repository, musicWebservice);
    }

    @Test
    void getLyricsFromMongoDBShouldReturnValue() throws IOException {
        String authToken = "test";
        String expectedLyrics = "abcd";
        int songId = 1;
        Song testSong = new Song("Title", "Artist");
        mockSongResponse(authToken, songId, testSong);
        SongLyrics songLyrics = new SongLyrics(songId, expectedLyrics, "");
        when(repository.findById(songId)).thenReturn(Optional.of(songLyrics));
        String lyrics = service.getLyrics(songId, authToken);
        assertEquals(expectedLyrics, lyrics);
    }

    @Test
    void getLyricsFromMusicWebServiceShouldReturnValue() throws IOException {
        String authToken = "test";
        String expectedLyrics = "abcd";
        String title = "Title";
        String artist = "Artist";
        int songId = 1;
        Song testSong = new Song(title, artist);
        mockSongResponse(authToken, songId, testSong);
        when(repository.findById(songId)).thenReturn(Optional.empty());
        Lyrics lyricsResponse = new Lyrics(1, expectedLyrics, "");
        when(musicWebservice.getLyrics(title, artist)).thenReturn(lyricsResponse);
        String lyrics = service.getLyrics(songId, authToken);
        assertEquals(expectedLyrics, lyrics);
    }

    @Test
    void getLyricsForNonExistingSongShouldThrowException() {
        String authToken = "test";
        int songId = 1;
        mockSongResponse(authToken, songId, null);
        assertThrows(IllegalArgumentException.class, () -> {
            service.getLyrics(songId, authToken);
        });
    }

    private void mockSongResponse(String authToken, int songId, Song expected) {
        ResponseEntity<Song> songResponse = Mockito.mock(ResponseEntity.class);
        when(songResponse.getBody()).thenReturn(expected);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        when(restTemplate.exchange("http://SONGS-SERVICE/songs/" + songId, HttpMethod.GET, request, Song.class)).thenReturn(songResponse);
    }
}