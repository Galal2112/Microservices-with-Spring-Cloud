package htwb.ai.lyricsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import htwb.ai.lyricsservice.common.Lyrics;
import htwb.ai.lyricsservice.common.Song;
import htwb.ai.lyricsservice.entity.SongLyrics;
import htwb.ai.lyricsservice.repository.SongLyricsRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@AllArgsConstructor
public class LyricsService {

    @Autowired
    @LoadBalanced
    private final RestTemplate template;
    private final SongLyricsRepository repository;
    private final MusixmatchService musicWebservice;

    public String getLyrics(int songId, String authorization) throws JsonProcessingException {
        Song song = getSongById(songId, authorization);
        if (song == null) {
            throw new IllegalArgumentException();
        }
        Optional<SongLyrics> songLyrics = repository.findById(songId);
        if (songLyrics.isPresent()) {
            return songLyrics.get().getLyricsBody();
        }
        Lyrics lyrics = musicWebservice.getLyrics(song.getTitle(), song.getArtist());
        saveLyrics(lyrics.lyricsBody, lyrics.lyricsCopyright, songId);
        return lyrics.lyricsBody;
    }

    private Song getSongById(int songId, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", authorization);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Song> response = template.exchange("http://SONGS-SERVICE/songs/" + songId, HttpMethod.GET, request, Song.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private void saveLyrics(String lyricsBody, String lyricsCopyright, int songId) {
        SongLyrics songLyrics = new SongLyrics();
        songLyrics.setLyricsBody(lyricsBody);
        songLyrics.setLyricsCopyright(lyricsCopyright);
        songLyrics.setId(songId);
        repository.save(songLyrics);
    }
}
